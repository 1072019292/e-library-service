package dev.lizhi.elibrary.lending;

import dev.lizhi.elibrary.catalog.Book;
import dev.lizhi.elibrary.catalog.BookRepository;
import dev.lizhi.elibrary.common.ApiException;
import dev.lizhi.elibrary.common.PageResponse;
import dev.lizhi.elibrary.common.Pagination;
import dev.lizhi.elibrary.identity.UserRepository;
import java.time.Clock;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LendingService {
    private final BookRepository books;
    private final LoanRepository loans;
    private final UserRepository users;
    private final Clock clock;

    public LendingService(BookRepository books, LoanRepository loans, UserRepository users, Clock clock) {
        this.books = books;
        this.loans = loans;
        this.users = users;
        this.clock = clock;
    }

    @Transactional
    public LoanView borrow(long userId, long bookId) {
        // 所有借还写入先锁同一书籍行，写入一致性依赖数据库，而非单 JVM 的 synchronized。
        Book book = lockBook(bookId);
        if (loans.existsByUserIdAndBookIdAndReturnedAtIsNull(userId, bookId)) {
            throw new ApiException(HttpStatus.CONFLICT, "ALREADY_BORROWED", "You already have an active loan for this book.");
        }
        if (loans.countByBookIdAndReturnedAtIsNull(bookId) >= book.getTotalLicenses()) {
            throw new ApiException(HttpStatus.CONFLICT, "NO_LICENSE_AVAILABLE", "All licenses for this book are currently borrowed.");
        }
        Loan loan = new Loan(users.getReferenceById(userId), book, clock.instant());
        return LoanView.from(loans.save(loan));
    }

    @Transactional
    public LoanView returnLoan(long userId, long loanId) {
        long bookId = loans.findOwnedBookId(loanId, userId).orElseThrow(this::loanNotFound);
        lockBook(bookId);
        Loan loan = loans.findOwned(loanId, userId).orElseThrow(this::loanNotFound);
        loan.returnAt(clock.instant());
        // 托管实体在事务提交时保存；重复归还不会覆盖原归还时间。
        return LoanView.from(loan);
    }

    @Transactional(readOnly = true)
    public PageResponse<LoanView> current(long userId, int page, int size) {
        return PageResponse.from(loans.findCurrent(userId, Pagination.request(page, size)).map(LoanView::from));
    }

    private Book lockBook(long bookId) {
        return books.findForUpdate(bookId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                "BOOK_NOT_FOUND", "The book was not found."));
    }

    private ApiException loanNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "LOAN_NOT_FOUND", "The loan was not found.");
    }
}
