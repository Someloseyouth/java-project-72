package hexlet.code;

import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
    private static int getPort() {
        var port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        return Integer.valueOf(port);
    }

    public static Javalin getApp() {
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

    public static void main(String[] args) {
        log.info("Starting application...");
        var app = getApp();
        app.start();
        log.info("Application started");
    }
}
