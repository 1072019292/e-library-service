package dev.lizhi.elibrary.library;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserIdAndBookId(long userId, long bookId);
    void deleteByUserIdAndBookId(long userId, long bookId);
    long countByUserId(long userId);
}
