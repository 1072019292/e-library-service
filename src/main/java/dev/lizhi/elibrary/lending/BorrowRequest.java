package dev.lizhi.elibrary.lending;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BorrowRequest(@NotNull @Positive Long bookId) { }
