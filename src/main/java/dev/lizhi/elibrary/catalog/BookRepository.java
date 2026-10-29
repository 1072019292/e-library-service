package dev.lizhi.elibrary.catalog;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {
    // 投影在同一条查询中取得授权与借阅数量，列表不逐本执行 count 查询。
    @Query(value = """
            select b.id as id, b.title as title, b.author as author, b.description as description,
                   b.totalLicenses as totalLicenses,
                   (select count(l.id) from Loan l where l.book = b and l.returnedAt is null) as activeLoans
            from Book b
            where :pattern = '' or lower(b.title) like :pattern escape '!' or lower(b.author) like :pattern escape '!'
            order by b.id
            """, countQuery = """
            select count(b.id) from Book b
            where :pattern = '' or lower(b.title) like :pattern escape '!' or lower(b.author) like :pattern escape '!'
            """)
    Page<BookSnapshot> browse(@Param("pattern") String pattern, Pageable pageable);

    @Query("""
            select b.id as id, b.title as title, b.author as author, b.description as description,
                   b.totalLicenses as totalLicenses,
                   (select count(l.id) from Loan l where l.book = b and l.returnedAt is null) as activeLoans
            from Book b where b.id = :id
            """)
    Optional<BookSnapshot> findSnapshot(@Param("id") long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findForUpdate(@Param("id") long id);

    interface BookSnapshot {
        Long getId();
        String getTitle();
        String getAuthor();
        String getDescription();
        int getTotalLicenses();
        long getActiveLoans();
    }
}
