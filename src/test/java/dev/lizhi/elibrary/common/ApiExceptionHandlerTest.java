package dev.lizhi.elibrary.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;

class ApiExceptionHandlerTest {
    @Test
    void unexpectedFailuresNeverReturnInternalErrorMessages() {
        ApiExceptionHandler handler = new ApiExceptionHandler(new Problems());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/books");
        ProblemDetail response = handler.unexpected(new IllegalStateException("internal database credential"), request).getBody();
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getDetail()).isEqualTo("An unexpected error occurred.");
        assertThat(response.getProperties()).containsEntry("code", "INTERNAL_ERROR");
        assertThat(response.toString()).doesNotContain("internal database credential", "IllegalStateException");
    }
}
