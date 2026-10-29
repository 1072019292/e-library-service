package dev.lizhi.elibrary.catalog;

public record BookView(long id, String title, String author, String description,
                       int totalLicenses, int availableLicenses) {
    static BookView from(BookRepository.BookSnapshot book) {
        return new BookView(book.getId(), book.getTitle(), book.getAuthor(), book.getDescription(),
                book.getTotalLicenses(), book.getTotalLicenses() - Math.toIntExact(book.getActiveLoans()));
    }
}
