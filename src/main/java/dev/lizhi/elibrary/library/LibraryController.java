package dev.lizhi.elibrary.library;

import dev.lizhi.elibrary.catalog.BookView;
import dev.lizhi.elibrary.common.PageResponse;
import dev.lizhi.elibrary.identity.LibraryPrincipal;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
@Validated
public class LibraryController {
    private final LibraryService library;
    public LibraryController(LibraryService library) { this.library = library; }

    @GetMapping("/library")
    public LibraryService.Summary summary(@AuthenticationPrincipal LibraryPrincipal reader) {
        return library.summary(reader.id());
    }

    @GetMapping("/favorites")
    public PageResponse<BookView> favorites(@AuthenticationPrincipal LibraryPrincipal reader,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return library.favorites(reader.id(), page, size);
    }

    @PutMapping("/favorites/{bookId}")
    public BookView save(@AuthenticationPrincipal LibraryPrincipal reader, @PathVariable @Positive long bookId) {
        return library.save(reader.id(), bookId);
    }

    @DeleteMapping("/favorites/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@AuthenticationPrincipal LibraryPrincipal reader, @PathVariable @Positive long bookId) {
        library.remove(reader.id(), bookId);
    }
}
