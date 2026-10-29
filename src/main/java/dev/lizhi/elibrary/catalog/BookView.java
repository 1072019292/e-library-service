package dev.lizhi.elibrary.catalog;

import java.util.Arrays;
import java.util.List;

public record BookView(long id, String title, String author, String description,
                       int totalLicenses, int availableLicenses, String category,
                       String audience, List<String> highlights, String coverUrl, String sourceUrl, boolean featured) {
    public static BookView from(BookRepository.BookSnapshot book) {
        return new BookView(book.getId(), book.getTitle(), book.getAuthor(), book.getDescription(),
                book.getTotalLicenses(), book.getTotalLicenses() - Math.toIntExact(book.getActiveLoans()),
                book.getCategory(), book.getAudience(), Arrays.stream(book.getHighlights().split("\\n"))
                        .filter(value -> !value.isBlank()).toList(), book.getCoverUrl(), book.getSourceUrl(), book.getFeatured());
    }
}
