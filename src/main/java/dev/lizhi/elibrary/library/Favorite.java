package dev.lizhi.elibrary.library;

import dev.lizhi.elibrary.catalog.Book;
import dev.lizhi.elibrary.identity.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "favorites", uniqueConstraints = @UniqueConstraint(name = "ux_favorite_user_book", columnNames = {"user_id", "book_id"}))
public class Favorite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    @Column(nullable = false)
    private Instant createdAt;

    protected Favorite() { }

    public Favorite(User user, Book book, Instant createdAt) {
        this.user = user; this.book = book; this.createdAt = createdAt;
    }
}
