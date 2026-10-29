package dev.lizhi.elibrary;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.springframework.test.context.DynamicPropertyRegistry;

final class PostgresTestDatabase {
    private static final EmbeddedPostgres DATABASE = start();

    private PostgresTestDatabase() { }

    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> DATABASE.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
    }

    // 仅供仓库外的交付验收脚本启动原生数据库，应用 JAR 不包含此类。
    public static void main(String[] args) throws Exception {
        try (var connection = DATABASE.getPostgresDatabase().getConnection();
                var statement = connection.createStatement()) {
            statement.execute("create database deployment_smoke");
        }
        System.out.println("PG_JDBC_URL=" + DATABASE.getJdbcUrl("postgres", "postgres"));
        System.out.println("PG_EMPTY_JDBC_URL=" + DATABASE.getJdbcUrl("postgres", "deployment_smoke"));
        System.out.flush();
        try {
            System.in.read();
        } finally {
            DATABASE.close();
        }
    }

    private static EmbeddedPostgres start() {
        try {
            // Windows initdb 不能可靠处理含中文的安装/数据路径，独立运行目录不改变源码位置。
            Path root = Path.of(System.getProperty("elibrary.pg.test.root",
                    Path.of(System.getProperty("java.io.tmpdir"), "elibrary-postgres-tests").toString()));
            Files.createDirectories(root);
            Path data = Files.createTempDirectory(root, "database-");
            Path binaries = Files.createDirectories(root.resolve("binaries"));
            return EmbeddedPostgres.builder()
                    .setDataDirectory(data)
                    .setOverrideWorkingDirectory(binaries.toFile())
                    .setCleanDataDirectory(false)
                    .setPGStartupWait(Duration.ofSeconds(30))
                    .setLocaleConfig("locale", "C")
                    .setLocaleConfig("encoding", "UTF-8")
                    .setServerConfig("listen_addresses", "127.0.0.1")
                    .setServerConfig("lock_timeout", "3s")
                    .setServerConfig("synchronous_commit", "on")
                    .start();
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot start the isolated PostgreSQL test database.", exception);
        }
    }
}
