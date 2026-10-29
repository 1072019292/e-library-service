package dev.lizhi.elibrary.catalog;

import dev.lizhi.elibrary.common.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@Validated
public class BookController {
    private final CatalogService catalog;

    public BookController(CatalogService catalog) { this.catalog = catalog; }

    @GetMapping
    public PageResponse<BookView> browse(@RequestParam(defaultValue = "") @Size(max = 200) String q,
            @RequestParam(defaultValue = "") @Size(max = 40) String category,
            @RequestParam(defaultValue = "false") boolean availableOnly,
            @RequestParam(defaultValue = "false") boolean featuredOnly,
            @RequestParam(defaultValue = "catalog") @Pattern(regexp = "catalog|title|recent") String sort,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return catalog.browse(q, category, availableOnly, featuredOnly, sort, page, size);
    }

    @GetMapping("/categories")
    public List<CatalogService.CategoryView> categories() { return catalog.categories(); }

    @GetMapping("/{bookId}")
    public BookView detail(@PathVariable @Positive long bookId) { return catalog.detail(bookId); }
}
