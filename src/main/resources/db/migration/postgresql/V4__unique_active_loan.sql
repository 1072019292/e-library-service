CREATE UNIQUE INDEX ux_loan_active_user_book ON loans(user_id, book_id)
    WHERE returned_at IS NULL;
