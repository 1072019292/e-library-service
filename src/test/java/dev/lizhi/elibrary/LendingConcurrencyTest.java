package dev.lizhi.elibrary;

import static org.assertj.core.api.Assertions.assertThat;

import dev.lizhi.elibrary.common.ApiException;
import dev.lizhi.elibrary.identity.User;
import dev.lizhi.elibrary.lending.LoanView;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class LendingConcurrencyTest extends IntegrationSupport {
    @Test
    void eightReadersCompetingForOneLicenseProduceExactlyOneCommittedLoan() throws Exception {
        List<Callable<Outcome>> requests = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            long userId = users.save(new User("reader-" + i, ALICE_HASH)).getId();
            requests.add(() -> attemptBorrow(userId, 2));
        }
        List<Outcome> outcomes = parallel(requests);
        assertThat(outcomes.stream().filter(Outcome::success)).hasSize(1);
        assertThat(outcomes.stream().filter(outcome -> !outcome.success()).map(Outcome::code))
                .containsOnly("NO_LICENSE_AVAILABLE");
        assertThat(loans.countByBookIdAndReturnedAtIsNull(2)).isEqualTo(1);
    }

    @Test
    void concurrentDuplicateRequestsCannotConsumeTwoLicenses() throws Exception {
        List<Callable<Outcome>> requests = new ArrayList<>();
        for (int i = 0; i < 8; i++) { requests.add(() -> attemptBorrow(1, 1)); }
        List<Outcome> outcomes = parallel(requests);
        assertThat(outcomes.stream().filter(Outcome::success)).hasSize(1);
        assertThat(outcomes.stream().filter(outcome -> !outcome.success()).map(Outcome::code))
                .containsOnly("ALREADY_BORROWED");
        assertThat(loans.countByBookIdAndReturnedAtIsNull(1)).isEqualTo(1);
    }

    @Test
    void concurrentReturnsKeepOneTimestampAndReleaseOneLicense() throws Exception {
        long loanId = lending.borrow(1, 2).id();
        List<Callable<LoanView>> requests = new ArrayList<>();
        for (int i = 0; i < 8; i++) { requests.add(() -> lending.returnLoan(1, loanId)); }
        List<LoanView> returned = parallel(requests);
        assertThat(returned).extracting(LoanView::returnedAt).doesNotContainNull();
        assertThat(returned.stream().map(LoanView::returnedAt).distinct()).hasSize(1);
        assertThat(loans.countByBookIdAndReturnedAtIsNull(2)).isZero();
        lending.borrow(2, 2);
        assertThat(loans.countByBookIdAndReturnedAtIsNull(2)).isEqualTo(1);
        assertThat(attemptBorrow(1, 2).code()).isEqualTo("NO_LICENSE_AVAILABLE");
    }

    @Test
    void returnAndNewBorrowAreConsistentRegardlessOfLockOrder() throws Exception {
        long loanId = lending.borrow(1, 2).id();
        List<Outcome> outcomes = parallel(List.of(
                () -> { lending.returnLoan(1, loanId); return new Outcome(true, "RETURNED"); },
                () -> attemptBorrow(2, 2)));
        Outcome borrower = outcomes.get(1);
        if (!borrower.success()) {
            assertThat(borrower.code()).isEqualTo("NO_LICENSE_AVAILABLE");
            lending.borrow(2, 2);
        }
        assertThat(loans.countByBookIdAndReturnedAtIsNull(2)).isEqualTo(1);
        Instant returnedAt = loans.findById(loanId).orElseThrow().getReturnedAt();
        assertThat(returnedAt).isNotNull();
    }

    private Outcome attemptBorrow(long userId, long bookId) {
        try {
            lending.borrow(userId, bookId);
            return new Outcome(true, "CREATED");
        } catch (ApiException exception) {
            assertThat(exception.status().value()).isEqualTo(409);
            return new Outcome(false, exception.code());
        }
    }

    private <T> List<T> parallel(List<Callable<T>> tasks) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        CountDownLatch ready = new CountDownLatch(tasks.size());
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<T>> futures = new ArrayList<>();
            for (Callable<T> task : tasks) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) { throw new IllegalStateException("Start gate timed out."); }
                    return task.call();
                }));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) { results.add(future.get(20, TimeUnit.SECONDS)); }
            return results;
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    private record Outcome(boolean success, String code) { }
}
