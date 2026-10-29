package dev.lizhi.elibrary.identity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LibraryPrincipalTest {
    @Test
    void loggingAPrincipalCannotExposeItsPasswordHash() {
        LibraryPrincipal principal = new LibraryPrincipal(1, "reader", "$2b$sensitive-hash");
        assertThat(principal.toString()).contains("reader").doesNotContain("sensitive-hash", "$2b$");
    }
}
