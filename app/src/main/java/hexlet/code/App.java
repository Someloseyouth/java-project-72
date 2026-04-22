package hexlet.code;

import hexlet.code.repository.BaseRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.sql.SQLException;


import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Slf4j
public class App {
    private static int getPort() {
        var port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        return Integer.valueOf(port);
    }

    private static String getJdbcUrl() {
        var jdbcUrl = System.getenv().getOrDefault("JDBC_DATABASE_URL", "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1");
        return jdbcUrl;
    }

    private static String readResourceFile(String fileName) throws IOException {
        var inputStream = App.class.getClassLoader().getResourceAsStream(fileName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    public static Javalin getApp() throws IOException, SQLException {
        var hikariConfig = new HikariConfig();
        var jdbcUrl = getJdbcUrl();
        hikariConfig.setJdbcUrl(jdbcUrl);
        log.info("Using JDBC URL: {}", jdbcUrl);

        var dataSource = new HikariDataSource(hikariConfig);
        var sql = readResourceFile("schema.sql");

        log.info(sql);
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute(sql);
        }
        BaseRepository.dataSource = dataSource;


        var port = getPort();
        log.info("Configuring Javalin application on port {}", port);

        var app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.jetty.port = port;
            config.routes.get("/", ctx -> ctx.result("Hello World"));
        });

        log.info("Javalin application configured");
        return app;
    }

    public static void main(String[] args) throws IOException, SQLException {
        log.info("Starting application...");
        var app = getApp();
        app.start();
        log.info("Application started");
    }
}
