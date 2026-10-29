package dev.lizhi.elibrary.lending;

import static org.assertj.core.api.Assertions.assertThat;

import dev.lizhi.elibrary.catalog.Book;
import dev.lizhi.elibrary.identity.User;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class LoanTest {
    @Test
    void returningAgainPreservesOriginalTimestamp() {
        Loan loan = loanAt("2026-01-01T00:00:00Z");
        loan.returnAt(Instant.parse("2026-01-01T01:00:00.123456789Z"));
        loan.returnAt(Instant.parse("2026-01-01T02:00:00Z"));
        assertThat(loan.getReturnedAt()).isEqualTo(Instant.parse("2026-01-01T01:00:00.123456Z"));
    }

    @Test
    void backwardsClockCannotProduceReturnBeforeBorrow() {
        Loan loan = loanAt("2026-01-01T01:00:00Z");
        loan.returnAt(Instant.parse("2026-01-01T00:00:00Z"));
        assertThat(loan.getReturnedAt()).isEqualTo(loan.getBorrowedAt());
    }

    private Loan loanAt(String timestamp) {
        return new Loan(new User("reader", "unused"), new Book("Title", "Author", "Description", 1),
                Instant.parse(timestamp));
    }
}
