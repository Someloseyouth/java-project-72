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

public class UrlsController {
    public static void index(Context ctx) throws SQLException {
        var urls = UrlRepository.getEntities();
        for (var url : urls) {
            var lastCheckOptional = UrlCheckRepository.findLastByUrlId(url.getId());
            if (lastCheckOptional.isPresent()) {
                var lastCheck = lastCheckOptional.get();
                url.setLastStatusCode(lastCheck.getStatusCode());
                url.setLastCheckAt(lastCheck.getCreatedAt());
            }
        }
        var page = new UrlsPage(urls);
        ctx.render("urls/index.jte", View.model("page", page));
    }

    public static void show(Context ctx) throws SQLException {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var urlOptional = UrlRepository.find(id);
        if (urlOptional.isEmpty()) {
            throw new NotFoundResponse("Url not found");
        }

        var url = urlOptional.get();
        var checks = UrlCheckRepository.findByUrlId(id);
        var page = new UrlPage(url, checks);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));
        ctx.render("urls/show.jte", View.model("page", page));
    }

    public static void check(Context ctx) throws SQLException {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var urlOptional = UrlRepository.find(id);
        if (urlOptional.isEmpty()) {
            throw new NotFoundResponse("Url not found");
        }
        var url = urlOptional.get();
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
        var urlName = ctx.formParamAsClass("url", String.class).get();
        var normalized = urlName.strip().trim();
        String dbUrl;
        if (normalized.isEmpty()) {
            ctx.status(422);
            var page = new IndexPage();
            page.setUrl(normalized);
            page.setFlash("Некорректный URL");
            page.setFlashType("danger");
            ctx.render("index.jte", View.model("page", page));
            return;
        }

        try {
            var uri = new URI(normalized);
            var url = uri.toURL();
            var urlProtocol = url.getProtocol();
            var urlHost = url.getHost();
            var urlPort = url.getPort();
            if (urlPort == -1) {
                dbUrl = urlProtocol + "://" + urlHost;
            } else {
                dbUrl = urlProtocol + "://" + urlHost + ":" + urlPort;
            }
            var urlOptional = UrlRepository.findByName(dbUrl);
            if (urlOptional.isEmpty()) {
                var urlEntity = new Url(dbUrl);
                UrlRepository.save(urlEntity);
                ctx.sessionAttribute("flash", "Страница успешно добавлена");
                ctx.sessionAttribute("flashType", "success");
                ctx.redirect(NamedRoutes.urlPath(urlEntity.getId()));
            } else {
                Url existing = urlOptional.get();
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("flashType", "warning");
                ctx.redirect(NamedRoutes.urlPath(existing.getId()));
            }
        } catch (Exception e) {
            ctx.status(422);
            var page = new IndexPage();
            page.setUrl(normalized);
            page.setFlash("Некорректный URL");
            page.setFlashType("danger");
            ctx.render("index.jte", View.model("page", page));
        }
    }
}
