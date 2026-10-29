package dev.lizhi.elibrary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import dev.lizhi.elibrary.library.LibraryService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

class LibraryFeaturesTest extends IntegrationSupport {
    @Autowired LibraryService library;

    @BeforeEach
    void metadata() {
        jdbc.update("update books set category = ?, audience = ?, highlights = ?, featured = true where id = 1",
                "数据库与系统", "后端工程师", "复制与分区\n事务一致性");
        jdbc.update("update books set category = ? where id = 2", "后端与 Java");
        jdbc.update("update books set category = ? where id = 3", "架构与工程");
    }

    @Test
    void categoryFiltersAndMetadataAreRealCatalogData() throws Exception {
        mvc.perform(get("/api/books").param("category", "数据库与系统"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].category").value("数据库与系统"))
                .andExpect(jsonPath("$.items[0].audience").value("后端工程师"))
                .andExpect(jsonPath("$.items[0].highlights[1]").value("事务一致性"));
        mvc.perform(get("/api/books/categories")).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("数据库与系统", "后端与 Java", "架构与工程")));
        mvc.perform(get("/api/books").param("q", "事务一致性"))
                .andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.items[0].id").value(1));
    }

    @Test
    void availabilityFilterReflectsCommittedBorrowAndReturn() throws Exception {
        long loan = lending.borrow(1, 2).id();
        mvc.perform(get("/api/books").param("availableOnly", "true"))
                .andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(get("/api/books").param("availableOnly", "true").param("category", "后端与 Java"))
                .andExpect(jsonPath("$.totalElements").value(0));
        lending.returnLoan(1, loan);
        mvc.perform(get("/api/books").param("availableOnly", "true"))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void sortingAndFeaturedSelectionRemainPagedAndDeterministic() throws Exception {
        mvc.perform(get("/api/books").param("sort", "title")).andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(1)).andExpect(jsonPath("$.items[1].id").value(3));
        mvc.perform(get("/api/books").param("sort", "recent").param("size", "1"))
                .andExpect(jsonPath("$.items[0].id").value(3)).andExpect(jsonPath("$.totalElements").value(3));
        mvc.perform(get("/api/books").param("featuredOnly", "true"))
                .andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.items[0].id").value(1));
        mvc.perform(get("/api/books").param("sort", "unknown")).andExpect(status().isBadRequest());
    }

    @Test
    void favoritesAreIdempotentAndPrivate() throws Exception {
        for (int i = 0; i < 2; i++) {
            mvc.perform(put("/api/me/favorites/1").with(httpBasic("alice", "alice-demo-pass")))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
        }
        mvc.perform(get("/api/me/favorites").with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/me/favorites").param("userId", "1").with(httpBasic("bob", "bob-demo-pass")))
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/me/library").with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(jsonPath("$.favoriteBooks").value(1));
        assertThat(jdbc.queryForObject("select count(*) from favorites", Long.class)).isEqualTo(1);
    }

    @Test
    void removingAFavoriteIsIdempotentAndDoesNotReturnABorrowedBook() throws Exception {
        lending.borrow(1, 1);
        library.save(1, 1);
        for (int i = 0; i < 2; i++) {
            mvc.perform(delete("/api/me/favorites/1").with(httpBasic("alice", "alice-demo-pass")))
                    .andExpect(status().isNoContent());
        }
        assertThat(loans.countByBookIdAndReturnedAtIsNull(1)).isEqualTo(1);
        assertThat(library.summary(1).favoriteBooks()).isZero();
    }

    @Test
    void historyPreservesReturnedLoansWithoutLeakingAnotherReadersRecords() throws Exception {
        long loanId = lending.borrow(1, 2).id();
        lending.returnLoan(1, loanId);
        mvc.perform(get("/api/me/loans").with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/me/loans").param("status", "returned").with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].id").value(loanId))
                .andExpect(jsonPath("$.items[0].returnedAt").isNotEmpty());
        mvc.perform(get("/api/me/loans").param("status", "returned").with(httpBasic("bob", "bob-demo-pass")))
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/me/library").with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(jsonPath("$.activeLoans").value(0)).andExpect(jsonPath("$.returnedLoans").value(1));
    }

    @Test
    void newPersonalEndpointsRequireAuthenticationAndValidateInputs() throws Exception {
        mvc.perform(get("/api/me/favorites")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/me/library")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/me/favorites/1")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/me/favorites/1")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/me/favorites/999999").with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/me/favorites").param("size", "0").with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/me/loans").param("status", "unknown").with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void concurrentSavingAndDatabaseConstraintPreventDuplicateFavorites() throws Exception {
        var executor = Executors.newFixedThreadPool(8);
        var ready = new CountDownLatch(8);
        var start = new CountDownLatch(1);
        List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < 8; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    try { if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Start timed out"); }
                    catch (InterruptedException error) { Thread.currentThread().interrupt(); throw new IllegalStateException(error); }
                    library.save(1, 1);
                }));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            for (var future : futures) future.get(20, TimeUnit.SECONDS);
        } finally {
            start.countDown(); executor.shutdownNow(); executor.awaitTermination(5, TimeUnit.SECONDS);
        }
        assertThat(library.summary(1).favoriteBooks()).isEqualTo(1);
        assertThatThrownBy(() -> jdbc.update("insert into favorites(user_id,book_id,created_at) values (1,1,current_timestamp)"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
