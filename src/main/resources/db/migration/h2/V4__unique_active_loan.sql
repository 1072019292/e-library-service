-- H2 has no PostgreSQL-style partial index. NULLs remain distinct in a unique index.
ALTER TABLE loans ADD active_user_id BIGINT
    GENERATED ALWAYS AS (CASE WHEN returned_at IS NULL THEN user_id ELSE NULL END);
CREATE UNIQUE INDEX ux_loan_active_user_book ON loans(active_user_id, book_id);
