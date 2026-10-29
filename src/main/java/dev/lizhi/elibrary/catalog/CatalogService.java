package dev.lizhi.elibrary.catalog;

import dev.lizhi.elibrary.common.ApiException;
import dev.lizhi.elibrary.common.PageResponse;
import dev.lizhi.elibrary.common.Pagination;
import java.util.Locale;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CatalogService {
    private final BookRepository books;

    public CatalogService(BookRepository books) { this.books = books; }

    public PageResponse<BookView> browse(String keyword, String category, boolean availableOnly,
            boolean featuredOnly, String sort, int page, int size) {
        String normalized = keyword.strip().toLowerCase(Locale.ROOT);
        // 用户的 % 和 _ 按字面量搜索，不作为 SQL LIKE 通配符。
        String pattern = normalized.isEmpty() ? "" : "%" + normalized.replace("!", "!!")
                .replace("%", "!%").replace("_", "!_") + "%";
        return PageResponse.from(books.browse(pattern, category.strip(), availableOnly, featuredOnly,
                sort, Pagination.request(page, size)).map(BookView::from));
    }

    public BookView detail(long bookId) {
        return books.findSnapshot(bookId).map(BookView::from).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "BOOK_NOT_FOUND", "The book was not found."));
    }

    public List<CategoryView> categories() {
        return books.categories().stream().map(value -> new CategoryView(value.getName(), value.getBookCount())).toList();
    }

    public record CategoryView(String name, long bookCount) { }
}
