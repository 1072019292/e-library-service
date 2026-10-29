package dev.lizhi.elibrary.library;

import dev.lizhi.elibrary.catalog.Book;
import dev.lizhi.elibrary.catalog.BookRepository;
import dev.lizhi.elibrary.catalog.BookView;
import dev.lizhi.elibrary.common.ApiException;
import dev.lizhi.elibrary.common.PageResponse;
import dev.lizhi.elibrary.common.Pagination;
import dev.lizhi.elibrary.identity.UserRepository;
import dev.lizhi.elibrary.lending.LoanRepository;
import java.time.Clock;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LibraryService {
    private final BookRepository books;
    private final FavoriteRepository favorites;
    private final UserRepository users;
    private final LoanRepository loans;
    private final Clock clock;

    public LibraryService(BookRepository books, FavoriteRepository favorites, UserRepository users,
            LoanRepository loans, Clock clock) {
        this.books = books; this.favorites = favorites; this.users = users; this.loans = loans; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<BookView> favorites(long userId, int page, int size) {
        return PageResponse.from(books.favorites(userId, Pagination.request(page, size)).map(BookView::from));
    }

    @Transactional
    public BookView save(long userId, long bookId) {
        // 与借还使用相同的书籍锁顺序，避免并发收藏重复插入及相反锁序。
        Book book = lockBook(bookId);
        if (!favorites.existsByUserIdAndBookId(userId, bookId)) {
            favorites.save(new Favorite(users.getReferenceById(userId), book, clock.instant()));
        }
        return BookView.from(books.findSnapshot(bookId).orElseThrow());
    }

    @Transactional
    public void remove(long userId, long bookId) {
        lockBook(bookId);
        favorites.deleteByUserIdAndBookId(userId, bookId);
    }

    @Transactional(readOnly = true)
    public Summary summary(long userId) {
        return new Summary(loans.countByUserIdAndReturnedAtIsNull(userId),
                loans.countByUserIdAndReturnedAtIsNotNull(userId), favorites.countByUserId(userId));
    }

    private Book lockBook(long bookId) {
        return books.findForUpdate(bookId).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "BOOK_NOT_FOUND", "The book was not found."));
    }

    public record Summary(long activeLoans, long returnedLoans, long favoriteBooks) { }
}
