package dev.lizhi.elibrary;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

class FileCommitDurabilityTest {
    @TempDir Path directory;

    @Test
    void committedRecordSurvivesImmediateWriterProcessHaltUsingDefaultFileOptions() throws Exception {
        Map<?, ?> config;
        try (var input = getClass().getResourceAsStream("/application.yml")) {
            config = new Yaml().load(input);
        }
        Map<?, ?> spring = (Map<?, ?>) config.get("spring");
        Map<?, ?> datasource = (Map<?, ?>) spring.get("datasource");
        String configured = (String) datasource.get("url");
        String defaults = configured.substring("${DB_URL:".length(), configured.length() - 1);
        String options = defaults.substring(defaults.indexOf(';'));
        String url = "jdbc:h2:file:" + directory.resolve("committed").toString().replace('\\', '/') + options;
        String executable = Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java").toString();
        String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        var writer = new ProcessBuilder(executable, "-cp", classpath, CrashWriter.class.getName(), url)
                .redirectErrorStream(true).redirectOutput(directory.resolve("writer.log").toFile()).start();
        try {
            assertThat(writer.waitFor(20, TimeUnit.SECONDS)).isTrue();
            assertThat(writer.exitValue()).isZero();
            try (var connection = DriverManager.getConnection(url, "sa", "");
                    var statement = connection.createStatement();
                    var result = statement.executeQuery("select count(*) from committed_marker")) {
                result.next();
                assertThat(result.getInt(1)).isEqualTo(1);
            }
        } finally {
            if (writer.isAlive()) writer.destroyForcibly().waitFor(5, TimeUnit.SECONDS);
        }
    }

    public static class CrashWriter {
        public static void main(String[] args) throws Exception {
            var connection = DriverManager.getConnection(args[0], "sa", "");
            connection.setAutoCommit(false);
            var statement = connection.createStatement();
            statement.execute("create table committed_marker(id integer primary key)");
            statement.executeUpdate("insert into committed_marker values (1)");
            connection.commit();
            // Deliberately skip close and shutdown hooks in this owned test JVM.
            Runtime.getRuntime().halt(0);
        }
    }
}
