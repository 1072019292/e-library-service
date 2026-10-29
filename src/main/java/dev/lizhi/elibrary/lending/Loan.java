package dev.lizhi.elibrary.lending;

import dev.lizhi.elibrary.catalog.Book;
import dev.lizhi.elibrary.identity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "loans")
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    @Column(nullable = false)
    private Instant borrowedAt;
    private Instant returnedAt;

    protected Loan() { }

    public Loan(User user, Book book, Instant borrowedAt) {
        this.user = user;
        this.book = book;
        // 与数据库的微秒精度一致，保证第一次响应与后续幂等响应时间值一致。
        this.borrowedAt = borrowedAt.truncatedTo(ChronoUnit.MICROS);
    }

    public void returnAt(Instant now) {
        if (returnedAt == null) {
            Instant timestamp = now.truncatedTo(ChronoUnit.MICROS);
            returnedAt = timestamp.isBefore(borrowedAt) ? borrowedAt : timestamp;
        }
    }

    public Long getId() { return id; }
    public Book getBook() { return book; }
    public Instant getBorrowedAt() { return borrowedAt; }
    public Instant getReturnedAt() { return returnedAt; }
}
