package dev.lizhi.elibrary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.lizhi.elibrary.catalog.BookRepository;
import dev.lizhi.elibrary.identity.UserRepository;
import dev.lizhi.elibrary.lending.LendingService;
import dev.lizhi.elibrary.lending.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class IntegrationSupport {
    static final String ALICE_HASH = "$2b$10$bD2VhKj1azvK/zUPvt0Zbuz1rj44XN//H/TDUQta6L6NSzAD25NS.";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired BookRepository books;
    @Autowired UserRepository users;
    @Autowired LoanRepository loans;
    @Autowired LendingService lending;

    @DynamicPropertySource
    static void selectDatabase(DynamicPropertyRegistry registry) {
        if ("postgres".equals(System.getProperty("test.database"))) {
            PostgresTestDatabase.configure(registry);
        }
    }

    // 刻意不在测试类上使用 @Transactional，服务调用必须真实提交，线程才能看见彼此的写入。
    @BeforeEach
    void resetFixtures() {
        jdbc.update("delete from favorites");
        jdbc.update("delete from loans");
        jdbc.update("delete from books");
        jdbc.update("delete from app_users");
        jdbc.execute("alter table app_users alter column id restart with 1000");
        jdbc.update("insert into app_users(id, username, password_hash) values (1, 'alice', ?)", ALICE_HASH);
        jdbc.update("insert into app_users(id, username, password_hash) values (2, 'bob', ?)",
                "$2b$10$aTD.q4nS/jKSsj6EIK70ve7InuwZz0sGakyi0kDV.J.k94YEFmmFW");
        jdbc.update("insert into books(id,title,author,description,total_licenses) values (1,?,?,?,2)",
                "Designing Data-Intensive Applications", "Martin Kleppmann", "Data systems");
        jdbc.update("insert into books(id,title,author,description,total_licenses) values (2,?,?,?,1)",
                "Effective Java", "Joshua Bloch", "Java practices");
        jdbc.update("insert into books(id,title,author,description,total_licenses) values (3,?,?,?,3)",
                "Domain-Driven Design", "Eric Evans", "Domain modelling");
    }

    JsonNode json(MvcResult result) throws Exception {
        return mapper.readTree(result.getResponse().getContentAsByteArray());
    }
}
