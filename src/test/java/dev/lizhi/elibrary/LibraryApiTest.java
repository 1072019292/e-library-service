package dev.lizhi.elibrary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class LibraryApiTest extends IntegrationSupport {
    @Test
    void rootProvidesAPublicBrowserEntryToTheCatalog() throws Exception {
        mvc.perform(get("/")).andExpect(status().isFound())
                .andExpect(header().string("Location", "/api/books"));
    }

    @Test
    void browseHasStablePaginationAndPublicDetails() throws Exception {
        mvc.perform(get("/api/books").param("size", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].id").value(1)).andExpect(jsonPath("$.items[1].id").value(2))
                .andExpect(jsonPath("$.items[0].availableLicenses").value(2))
                .andExpect(jsonPath("$.totalElements").value(3)).andExpect(jsonPath("$.totalPages").value(2));
        mvc.perform(get("/api/books").param("page", "1").param("size", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].id").value(3));
        mvc.perform(get("/api/books").param("page", "20"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items", hasSize(0)));
        mvc.perform(get("/api/books/2")).andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Effective Java"))
                .andExpect(jsonPath("$.description").value("Java practices"));
    }

    @ParameterizedTest
    @CsvSource({"JAVA,2", " joshua ,2", "EVANS,3"})
    void searchesTitleAndAuthorIgnoringCase(String keyword, int bookId) throws Exception {
        mvc.perform(get("/api/books").param("q", keyword)).andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1))).andExpect(jsonPath("$.items[0].id").value(bookId));
    }

    @ParameterizedTest
    @ValueSource(strings = {"%", "_", "!", "does not exist"})
    void searchesSpecialCharactersLiterally(String keyword) throws Exception {
        mvc.perform(get("/api/books").param("q", keyword)).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @ParameterizedTest
    @CsvSource({"page,-1", "page,invalid", "size,0", "size,101", "size,invalid"})
    void rejectsInvalidPagination(String name, String value) throws Exception {
        mvc.perform(get("/api/books").param(name, value)).andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void validatesSearchLengthAndIdentifiers() throws Exception {
        mvc.perform(get("/api/books").param("q", "a".repeat(201))).andExpect(status().isBadRequest());
        mvc.perform(get("/api/books/0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/books/abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/books/999999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"));
    }

    @Test
    void excessiveJpaOffsetsAreInputErrorsRatherThanServerFailures() throws Exception {
        mvc.perform(get("/api/books").param("page", "2147483647").param("size", "100"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mvc.perform(get("/api/me/loans").param("page", "2147483647").param("size", "100")
                        .with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void completeBorrowReturnAndBorrowAgainFlow() throws Exception {
        JsonNode borrowed = borrow(2, "alice", "alice-demo-pass");
        long loanId = borrowed.path("id").asLong();
        assertThat(borrowed.path("returnedAt").isNull()).isTrue();
        assertThat(borrowed.path("borrowedAt").asText()).endsWith("Z");
        mvc.perform(get("/api/books/2")).andExpect(jsonPath("$.availableLicenses").value(0));
        mvc.perform(get("/api/me/loans").with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].id").value(loanId))
                .andExpect(jsonPath("$.items[0].book.title").value("Effective Java"))
                .andExpect(jsonPath("$.items[0].passwordHash").doesNotExist())
                .andExpect(header().doesNotExist("Set-Cookie"));
        JsonNode returned = json(mvc.perform(put("/api/loans/{id}/return", loanId)
                        .with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(status().isOk()).andReturn());
        JsonNode repeated = json(mvc.perform(put("/api/loans/{id}/return", loanId)
                        .with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(status().isOk()).andReturn());
        assertThat(returned.path("returnedAt").asText()).isNotBlank();
        assertThat(repeated).isEqualTo(returned);
        mvc.perform(get("/api/me/loans").with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(jsonPath("$.items", hasSize(0)));
        mvc.perform(get("/api/books/2")).andExpect(jsonPath("$.availableLicenses").value(1));
        assertThat(borrow(2, "alice", "alice-demo-pass").path("id").asLong()).isNotEqualTo(loanId);
        assertThat(loans.count()).isEqualTo(2);
    }

    @Test
    void duplicateBorrowIsRejectedEvenWhenAnotherLicenseIsAvailable() throws Exception {
        borrow(1, "alice", "alice-demo-pass");
        mvc.perform(post("/api/loans").with(httpBasic("alice", "alice-demo-pass"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"bookId\":1}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("ALREADY_BORROWED"));
        assertThat(loans.countByBookIdAndReturnedAtIsNull(1)).isEqualTo(1);
    }

    @Test
    void exhaustedLicensesAreRejectedAndUnknownBookIsNotFound() throws Exception {
        borrow(2, "alice", "alice-demo-pass");
        mvc.perform(post("/api/loans").with(httpBasic("bob", "bob-demo-pass"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"bookId\":2}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("NO_LICENSE_AVAILABLE"));
        mvc.perform(post("/api/loans").with(httpBasic("bob", "bob-demo-pass"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"bookId\":999999}"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"));
    }

    @Test
    void currentLoansArePrivatePaginatedAndExcludeReturnedLoans() throws Exception {
        long first = borrow(1, "alice", "alice-demo-pass").path("id").asLong();
        long latest = borrow(3, "alice", "alice-demo-pass").path("id").asLong();
        borrow(2, "bob", "bob-demo-pass");
        mvc.perform(get("/api/me/loans").param("size", "1").with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].id").value(latest))
                .andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(put("/api/loans/{id}/return", first).with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/me/loans").with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(jsonPath("$.items", hasSize(1))).andExpect(jsonPath("$.items[0].id").value(latest));
        mvc.perform(get("/api/me/loans").param("size", "101").with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannotReturnAnotherReadersLoanAndDoesNotRevealItsExistence() throws Exception {
        long loanId = borrow(1, "alice", "alice-demo-pass").path("id").asLong();
        JsonNode hidden = json(mvc.perform(put("/api/loans/{id}/return", loanId)
                        .with(httpBasic("bob", "bob-demo-pass")))
                .andExpect(status().isNotFound()).andReturn());
        JsonNode missing = json(mvc.perform(put("/api/loans/999999/return")
                        .with(httpBasic("bob", "bob-demo-pass")))
                .andExpect(status().isNotFound()).andReturn());
        assertThat(hidden.path("code").asText()).isEqualTo("LOAN_NOT_FOUND");
        assertThat(hidden.path("detail")).isEqualTo(missing.path("detail"));
        assertThat(loans.countByBookIdAndReturnedAtIsNull(1)).isEqualTo(1);
    }

    @Test
    void authenticationFailuresUseProblemDetailsAndNoSession() throws Exception {
        mvc.perform(get("/api/me/loans")).andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Basic realm=\"e-library\""))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mvc.perform(get("/api/me/loans").with(httpBasic("alice", "incorrect")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401)).andExpect(jsonPath("$.instance").value("/api/me/loans"))
                .andExpect(jsonPath("$.trace").doesNotExist()).andExpect(header().doesNotExist("Set-Cookie"));
        mvc.perform(post("/api/loans").contentType(MediaType.APPLICATION_JSON).content("{\"bookId\":1}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/api/loans/1/return")).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "null", "[]", "{\"bookId\":null}", "{\"bookId\":0}", "{\"bookId\":-1}",
            "{\"bookId\":1.2}", "{\"bookId\":\"oops\"}", "{\"bookId\":1,\"userId\":2}", "{broken",
            "{\"bookId\":1}{\"bookId\":2}"})
    void rejectsInvalidJsonAndCallerSuppliedIdentity(String body) throws Exception {
        mvc.perform(post("/api/loans").with(httpBasic("alice", "alice-demo-pass"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        assertThat(loans.count()).isZero();
    }

    @Test
    void rejectsFormRequestsAndDoesNotAllowCrossOriginCredentialRequests() throws Exception {
        mvc.perform(post("/api/loans").with(httpBasic("alice", "alice-demo-pass"))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED).content("bookId=1"))
                .andExpect(status().isUnsupportedMediaType()).andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
        mvc.perform(options("/api/loans").header("Origin", "https://untrusted.example")
                        .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().is4xxClientError()).andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        mvc.perform(get("/api/books").header("Origin", "https://untrusted.example"))
                .andExpect(status().isOk()).andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void frameworkErrorsAlsoUseStableProblemCodes() throws Exception {
        mvc.perform(post("/api/books").with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(status().isMethodNotAllowed()).andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
        mvc.perform(get("/api/missing").with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void healthReadinessIsPublicWithoutExposingDatabaseDetails() throws Exception {
        mvc.perform(get("/actuator/health/readiness")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void oldReturnReplayCannotReleaseANewLoan() throws Exception {
        long oldLoan = borrow(2, "alice", "alice-demo-pass").path("id").asLong();
        mvc.perform(put("/api/loans/{id}/return", oldLoan).with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(status().isOk());
        borrow(2, "bob", "bob-demo-pass");
        mvc.perform(put("/api/loans/{id}/return", oldLoan).with(httpBasic("alice", "alice-demo-pass")))
                .andExpect(status().isOk());
        assertThat(loans.countByBookIdAndReturnedAtIsNull(2)).isEqualTo(1);
        mvc.perform(get("/api/books/2")).andExpect(jsonPath("$.availableLicenses").value(0));
    }

    @Test
    void databaseLockWaitIsBoundedAndReturnsRetryableProblem() throws Exception {
        try (Connection blocker = jdbc.getDataSource().getConnection()) {
            blocker.setAutoCommit(false);
            try (PreparedStatement statement = blocker.prepareStatement("select id from books where id = 2 for update")) {
                statement.executeQuery().close();
                mvc.perform(post("/api/loans").with(httpBasic("alice", "alice-demo-pass"))
                                .contentType(MediaType.APPLICATION_JSON).content("{\"bookId\":2}"))
                        .andExpect(status().isServiceUnavailable()).andExpect(header().string("Retry-After", "1"))
                        .andExpect(jsonPath("$.code").value("BOOK_BUSY"));
            } finally {
                blocker.rollback();
            }
        }
        assertThat(loans.count()).isZero();
        assertThat(borrow(2, "alice", "alice-demo-pass").path("id").asLong()).isPositive();
    }

    private JsonNode borrow(long bookId, String username, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/loans").with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"bookId\":" + bookId + "}"))
                .andExpect(status().isCreated()).andReturn();
        return json(result);
    }
}
