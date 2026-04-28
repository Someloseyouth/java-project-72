package hexlet.code;


import static org.assertj.core.api.Assertions.assertThat;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.testtools.JavalinTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;


public class AppTest {
    private Javalin app;
    private static MockWebServer mockServer;

    @BeforeAll
    static void startServer() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterAll
    static void stopServer() throws IOException {
        mockServer.shutdown();
    }

    @BeforeEach
    public final void setUp() throws IOException, SQLException {
        app = App.getApp();
    }

    @Test
    public void testMainPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.indexPath());
            assertThat(response.code()).isEqualTo(HttpStatus.OK.getCode());
            assertThat(response.body().string()).contains("name=\"url\"");
        });
    }

    @Test
    public void testCreateUrl() {
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=https://ru.hexlet.io/blog/posts/operativnaya-pamyat-kesh";
            var response = client.post(NamedRoutes.urlsPath(), requestBody);
            assertThat(response.code()).isEqualTo(HttpStatus.FOUND.getCode());
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
            assertThat(response1.code()).isEqualTo(HttpStatus.FOUND.getCode());
            assertThat(response2.code()).isEqualTo(HttpStatus.FOUND.getCode());
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
            assertThat(response.code()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.getCode());
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
            assertThat(response.code()).isEqualTo(HttpStatus.OK.getCode());
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
            assertThat(response.code()).isEqualTo(HttpStatus.OK.getCode());
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
            assertThat(response.code()).isEqualTo(HttpStatus.NOT_FOUND.getCode());
        });
    }

    @Test
    public void testUrlCheck() throws SQLException {
        var html = """
                <html>
                  <head>
                    <title>Example Title</title>
                    <meta name="description" content="Example description">
                  </head>
                  <body>
                    <h1>Example H1</h1>
                  </body>
                </html>
                """;
        mockServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.getCode()).setBody(html));

        String urlToCheck = mockServer.url("/").toString();
        var url = new Url(urlToCheck);
        UrlRepository.save(url);

        JavalinTest.test(app, (server, client) -> {
            var response = client.post(NamedRoutes.urlCheckPath(url.getId()));
            assertThat(response.code()).isEqualTo(HttpStatus.FOUND.getCode());

            var showResponse = client.get(NamedRoutes.urlPath(url.getId()));
            var body = showResponse.body().string();
            assertThat(body).contains("data-test=\"checks\"");
            assertThat(body).contains("Example Title");
            assertThat(body).contains("Example H1");
            assertThat(body).contains("Example description");
        });
        var checks = UrlCheckRepository.findByUrlId(url.getId());
        assertThat(checks).hasSize(1);
        var check = checks.getFirst();
        assertThat(check.getStatusCode()).isEqualTo(HttpStatus.OK.getCode());
        assertThat(check.getTitle()).isEqualTo("Example Title");
        assertThat(check.getH1()).isEqualTo("Example H1");
        assertThat(check.getDescription()).isEqualTo("Example description");
    }

    @Test
    public void testFailedUrlCheck() throws SQLException {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.getCode()).setBody("Server error"));

        String urlToCheck = mockServer.url("/").toString();
        var url = new Url(urlToCheck);
        UrlRepository.save(url);

        JavalinTest.test(app, (server, client) -> {
            var response = client.post(NamedRoutes.urlCheckPath(url.getId()));
            assertThat(response.code()).isEqualTo(HttpStatus.FOUND.getCode());

            var showResponse = client.get(NamedRoutes.urlPath(url.getId()));
            var body = showResponse.body().string();
            assertThat(body).contains("Произошла ошибка при проверке");
        });

        var checks = UrlCheckRepository.findByUrlId(url.getId());
        assertThat(checks).isEmpty();
    }
}
