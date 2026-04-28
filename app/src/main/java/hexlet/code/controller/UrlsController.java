package hexlet.code.controller;

import hexlet.code.dto.IndexPage;
import hexlet.code.dto.UrlPage;
import hexlet.code.dto.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import hexlet.code.util.View;
import hexlet.code.util.TextUtils;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestException;
import org.jsoup.Jsoup;


import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UrlsController {
    public static void index(Context ctx) throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        var urls = UrlRepository.getEntities();
        var checks = UrlCheckRepository.findLastChecks();
        for (var url : urls) {
            var lastCheck = checks.get(url.getId());
            if (lastCheck != null) {
                url.setLastStatusCode(lastCheck.getStatusCode());
                var formatted = lastCheck.getCreatedAt().format(formatter);
                url.setLastCheckAtFormatted(formatted);
                url.setLastCheckAt(lastCheck.getCreatedAt());
            }
        }
        var page = new UrlsPage(urls);
        ctx.render("urls/index.jte", View.model("page", page));
    }

    public static void show(Context ctx) throws SQLException {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var url = UrlRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse("Url not found"));
        var checks = UrlCheckRepository.findByUrlId(id);
        var page = new UrlPage(url, checks);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));
        ctx.render("urls/show.jte", View.model("page", page));
    }

    public static void check(Context ctx) throws SQLException {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var url = UrlRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse("Url not found"));
        try {
            var response = Unirest.get(url.getName()).asString();
            var statusCode = response.getStatus();
            if (statusCode >= 400) {
                ctx.sessionAttribute("flash", "Произошла ошибка при проверке");
                ctx.sessionAttribute("flashType", "danger");
            } else {
                var body = response.getBody();
                var doc = Jsoup.parse(body);
                var h1Element = doc.selectFirst("h1");
                var h1 = h1Element != null ? h1Element.text() : null;
                var title = doc.title();
                var descriptionElement = doc.selectFirst("meta[name=description]");
                var description = descriptionElement != null
                        ? descriptionElement.attr("content") : null;
                h1 = TextUtils.shorten(h1);
                title = TextUtils.shorten(title);
                description = TextUtils.shorten(description);
                var check = new UrlCheck();
                check.setUrlId(id);
                check.setStatusCode(statusCode);
                check.setH1(h1);
                check.setTitle(title);
                check.setDescription(description);
                check.setCreatedAt(LocalDateTime.now());
                UrlCheckRepository.save(check);
                ctx.sessionAttribute("flash", "Страница успешно проверена");
                ctx.sessionAttribute("flashType", "success");
            }
        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Произошла ошибка при проверке");
            ctx.sessionAttribute("flashType", "danger");
        }
        ctx.redirect(NamedRoutes.urlPath(id));
    }

    public static void create(Context ctx) throws SQLException {
        var inputUrl = ctx.formParam("url");
        if (inputUrl == null || inputUrl.isBlank()) {
            renderInvalidUrl(ctx);
            return;
        }
        URI parsedUrl;
        try {
            parsedUrl = new URI(inputUrl);
        } catch (Exception e) {
            renderInvalidUrl(ctx);
            return;
        }
        var scheme = parsedUrl.getScheme();
        var host = parsedUrl.getHost();
        if (scheme == null
                || (!scheme.equals("http") && !scheme.equals("https"))
                || host == null) {
            renderInvalidUrl(ctx);
            return;
        }
        String normalizedUrl = String
                .format(
                        "%s://%s%s",
                        parsedUrl.getScheme(),
                        parsedUrl.getHost(),
                        parsedUrl.getPort() == -1 ? "" : ":" + parsedUrl.getPort()
                )
                .toLowerCase();
        Url url = UrlRepository.findByName(normalizedUrl).orElse(null);

        if (url != null) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "info");
        } else {
            Url newUrl = new Url(normalizedUrl);
            UrlRepository.save(newUrl);
            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flash-type", "success");
        }
        ctx.redirect(NamedRoutes.urlsPath());
    }

    private static void renderInvalidUrl(Context ctx) {
        ctx.status(422);
        var page = new IndexPage();
        page.setFlash("Некорректный URL");
        page.setFlashType("danger");
        ctx.render("index.jte", View.model("page", page));
    }
}
