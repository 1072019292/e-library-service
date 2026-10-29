package dev.lizhi.elibrary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class DatabaseContractTest extends IntegrationSupport {
    @Test
    void databaseEnforcesOneActiveLoanAndAllowsRepeatedHistoricalLoans() throws Exception {
        try (Connection connection = jdbc.getDataSource().getConnection()) {
            System.out.printf("DATABASE_UNDER_TEST=%s %s%n", connection.getMetaData().getDatabaseProductName(),
                    connection.getMetaData().getDatabaseProductVersion());
        }
        long first = lending.borrow(1, 1).id();
        assertThatThrownBy(() -> jdbc.update(
                "insert into loans(user_id,book_id,borrowed_at) values (1,1,current_timestamp)"))
                .isInstanceOf(DataIntegrityViolationException.class);
        lending.returnLoan(1, first);
        long second = lending.borrow(1, 1).id();
        lending.returnLoan(1, second);
        lending.borrow(1, 1);
        assertThat(loans.count()).isEqualTo(3);
        assertThat(loans.countByBookIdAndReturnedAtIsNull(1)).isEqualTo(1);
    }

    @Test
    void databaseRejectsDanglingReferences() {
        assertThatThrownBy(() -> jdbc.update(
                "insert into loans(user_id,book_id,borrowed_at) values (999999,1,current_timestamp)"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseRejectsNonPositiveLicenseCapacity() {
        assertThatThrownBy(() -> jdbc.update("update books set total_licenses = 0 where id = 1"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
