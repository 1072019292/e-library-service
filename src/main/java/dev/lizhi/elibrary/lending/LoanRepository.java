package dev.lizhi.elibrary.lending;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    boolean existsByUserIdAndBookIdAndReturnedAtIsNull(long userId, long bookId);
    long countByBookIdAndReturnedAtIsNull(long bookId);
    long countByUserIdAndReturnedAtIsNull(long userId);
    long countByUserIdAndReturnedAtIsNotNull(long userId);

    // 先只读标量，再锁书，再读取 Loan，避免锁前加载的实体保留过期的 returnedAt。
    @Query("select l.book.id from Loan l where l.id = :loanId and l.user.id = :userId")
    Optional<Long> findOwnedBookId(@Param("loanId") long loanId, @Param("userId") long userId);

    @Query("select l from Loan l join fetch l.book where l.id = :loanId and l.user.id = :userId")
    Optional<Loan> findOwned(@Param("loanId") long loanId, @Param("userId") long userId);

    @Query(value = """
            select l from Loan l join fetch l.book
            where l.user.id = :userId and l.returnedAt is null
            order by l.borrowedAt desc, l.id desc
            """, countQuery = "select count(l.id) from Loan l where l.user.id = :userId and l.returnedAt is null")
    Page<Loan> findCurrent(@Param("userId") long userId, Pageable pageable);

    @Query(value = """
            select l from Loan l join fetch l.book
            where l.user.id = :userId and l.returnedAt is not null
            order by l.returnedAt desc, l.id desc
            """, countQuery = "select count(l.id) from Loan l where l.user.id = :userId and l.returnedAt is not null")
    Page<Loan> findReturned(@Param("userId") long userId, Pageable pageable);
}
