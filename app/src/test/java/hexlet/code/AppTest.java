package hexlet.code;


import static org.assertj.core.api.Assertions.assertThat;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;


public class AppTest {
    private Javalin app;

    @BeforeEach
    public final void setUp() throws IOException, SQLException {
        app = App.getApp();
    }

    @Test
    public void testMainPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.indexPath());
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("name=\"url\"");
        });
    }

    @Test
    public void testCreateUrl() {
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=https://ru.hexlet.io/blog/posts/operativnaya-pamyat-kesh";
            var response = client.post(NamedRoutes.urlsPath(), requestBody);
            assertThat(response.code()).isBetween(300, 399);
            var urls = UrlRepository.getEntities();
            assertThat(urls).hasSize(1);
            assertThat(urls.getFirst().getName()).isEqualTo("https://ru.hexlet.io");
        });
    }

    @Test
    public void testCreateExistingUrl() {
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=https://ru.hexlet.io/blog/posts/operativnaya-pamyat-kesh";
            var response1 = client.post(NamedRoutes.urlsPath(), requestBody);
            var response2 = client.post(NamedRoutes.urlsPath(), requestBody);
            assertThat(response1.code()).isBetween(300, 399);
            assertThat(response2.code()).isBetween(300, 399);
            var urls = UrlRepository.getEntities();
            assertThat(urls).hasSize(1);
            assertThat(urls.getFirst().getName()).isEqualTo("https://ru.hexlet.io");
        });
    }

    @Test
    public void testCreateInvalidUrl() {
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=not-a-url";
            var response = client.post(NamedRoutes.urlsPath(), requestBody);
            assertThat(response.code()).isEqualTo(422);
            var urls = UrlRepository.getEntities();
            assertThat(urls).hasSize(0);
            assertThat(response.body().string()).contains("Некорректный URL");
        });
    }

    @Test
    public void testUrlsList() throws SQLException {
        var url1 = new Url("https://ru.hexlet.io/blog/posts/operativnaya-pamyat-kesh");
        var url2 = new Url("https://mvnrepository.com/artifact/org.assertj/assertj-core/versions");
        UrlRepository.save(url1);
        UrlRepository.save(url2);
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urlsPath());
            assertThat(response.code()).isEqualTo(200);
            var body = response.body().string();
            assertThat(body).contains("data-test=\"urls\"");
            assertThat(body).contains("ru.hexlet.io");
            assertThat(body).contains("mvnrepository.com");
        });
    }

    @Test
    public void testUrlPage() throws SQLException {
        var url = new Url("https://ru.hexlet.io/blog/posts/operativnaya-pamyat-kesh");
        UrlRepository.save(url);
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urlPath(url.getId()));
            assertThat(response.code()).isEqualTo(200);
            var body = response.body().string();
            assertThat(body).contains("ru.hexlet.io");
            assertThat(body).contains("data-test=\"url\"");
            assertThat(body).contains("data-test=\"checks\"");
        });
    }

    @Test
    public void testUrlPageNotFound() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urlPath("99999999999"));
            assertThat(response.code()).isEqualTo(404);
        });
    }

}
