package hexlet.code;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.controller.UrlsController;
import hexlet.code.dto.IndexPage;
import hexlet.code.repository.BaseRepository;

import hexlet.code.util.NamedRoutes;
import hexlet.code.util.View;
import io.javalin.Javalin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.sql.SQLException;

import io.javalin.rendering.template.JavalinJte;
import lombok.extern.slf4j.Slf4j;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Slf4j
public class App {
    private static int getPort() {
        var port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        return port;
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

    private static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
        return templateEngine;
    }

    public static void main(String[] args) throws IOException, SQLException {
        log.info("Starting application...");
        var app = getApp();
        app.start();
        log.info("Application started");
    }

    public static Javalin getApp() throws IOException, SQLException {
        var hikariConfig = new HikariConfig();
        var jdbcUrl = getJdbcUrl();
        hikariConfig.setJdbcUrl(jdbcUrl);

        if (jdbcUrl.startsWith("jdbc:postgresql:")) {
            hikariConfig.setDriverClassName("org.postgresql.Driver");
        }

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
            config.fileRenderer(new JavalinJte(createTemplateEngine()));

            config.routes.get(NamedRoutes.indexPath(), ctx -> {
                var page = new IndexPage();
                ctx.render("index.jte", View.model("page", page));
            });

            config.routes.get(NamedRoutes.urlsPath(), UrlsController::index);
            config.routes.get(NamedRoutes.urlPath("{id}"), UrlsController::show);
            config.routes.post(NamedRoutes.urlsPath(), UrlsController::create);
            config.routes.post("/urls/{id}/checks",ctx -> {
                var id = ctx.pathParamAsClass("id", Long.class).get();
                ctx.redirect(NamedRoutes.urlPath(id));
            });
        });

        log.info("Javalin application configured");
        return app;
    }
}
