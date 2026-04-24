package hexlet.code.controller;

import hexlet.code.dto.IndexPage;
import hexlet.code.dto.UrlPage;
import hexlet.code.dto.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import hexlet.code.util.View;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

import java.net.URI;
import java.sql.SQLException;

public class UrlsController {
    public static void index(Context ctx) throws SQLException {
        var urls = UrlRepository.getEntities();
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
        var page = new UrlPage(url);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));
        ctx.render("urls/show.jte", View.model("page", page));
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
