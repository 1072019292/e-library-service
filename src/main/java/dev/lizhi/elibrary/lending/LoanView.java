package dev.lizhi.elibrary.lending;

import java.time.Instant;

public record LoanView(long id, BookReference book, Instant borrowedAt, Instant returnedAt) {
    static LoanView from(Loan loan) {
        return new LoanView(loan.getId(), new BookReference(loan.getBook().getId(),
                loan.getBook().getTitle(), loan.getBook().getAuthor()), loan.getBorrowedAt(), loan.getReturnedAt());
    }

    public record BookReference(long id, String title, String author) { }
}
