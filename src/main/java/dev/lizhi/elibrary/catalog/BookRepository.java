package dev.lizhi.elibrary.catalog;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {
    String SNAPSHOT = """
            select b.id as id, b.title as title, b.author as author, b.description as description,
                   b.category as category, b.audience as audience, b.highlights as highlights,
                   b.coverUrl as coverUrl, b.sourceUrl as sourceUrl, b.featured as featured, b.totalLicenses as totalLicenses,
                   (select count(l.id) from Loan l where l.book = b and l.returnedAt is null) as activeLoans
            """;
    String FILTER = """
            where (:pattern = '' or lower(b.title) like :pattern escape '!'
                   or lower(b.author) like :pattern escape '!' or lower(b.description) like :pattern escape '!'
                   or lower(b.category) like :pattern escape '!' or lower(b.audience) like :pattern escape '!'
                   or lower(b.highlights) like :pattern escape '!')
              and (:category = '' or b.category = :category)
              and (:featuredOnly = false or b.featured = true)
              and (:availableOnly = false or
                   (select count(l.id) from Loan l where l.book = b and l.returnedAt is null) < b.totalLicenses)
            """;
    // 投影在同一条查询中取得授权与借阅数量，列表不逐本执行 count 查询。
    @Query(value = SNAPSHOT + " from Book b " + FILTER + """
            order by case when :sort = 'title' then lower(b.title) end asc,
                     case when :sort = 'recent' then b.id end desc, b.id asc
            """, countQuery = "select count(b.id) from Book b " + FILTER)
    Page<BookSnapshot> browse(@Param("pattern") String pattern, @Param("category") String category,
            @Param("availableOnly") boolean availableOnly, @Param("featuredOnly") boolean featuredOnly,
            @Param("sort") String sort, Pageable pageable);

    @Query(SNAPSHOT + " from Book b where b.id = :id")
    Optional<BookSnapshot> findSnapshot(@Param("id") long id);

    @Query("select b.category as name, count(b.id) as bookCount from Book b group by b.category order by b.category")
    List<CategorySnapshot> categories();

    @Query(value = SNAPSHOT + """
            from Favorite f join f.book b where f.user.id = :userId
            order by f.createdAt desc, f.id desc
            """, countQuery = "select count(f.id) from Favorite f where f.user.id = :userId")
    Page<BookSnapshot> favorites(@Param("userId") long userId, Pageable pageable);

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
        String getCategory();
        String getAudience();
        String getHighlights();
        String getCoverUrl();
        String getSourceUrl();
        boolean getFeatured();
    }

    interface CategorySnapshot {
        String getName();
        long getBookCount();
    }
}
