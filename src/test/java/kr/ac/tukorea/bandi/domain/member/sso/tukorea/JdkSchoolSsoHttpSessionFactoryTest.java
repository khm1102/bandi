package kr.ac.tukorea.bandi.domain.member.sso.tukorea;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import kr.ac.tukorea.bandi.global.config.SchoolSsoProperties;
import kr.ac.tukorea.bandi.domain.member.exception.SchoolSsoResponseChangedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JdkSchoolSsoHttpSessionFactoryTest {

    private HttpServer server;
    private URI baseUri;
    private final AtomicReference<String> receivedCookie = new AtomicReference<>();
    private final AtomicReference<String> receivedForm = new AtomicReference<>();
    private final AtomicReference<String> receivedReferer = new AtomicReference<>();

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        baseUri = URI.create("http://127.0.0.1:" + server.getAddress().getPort());
        server.createContext("/login", this::loginPage);
        server.createContext("/process", this::loginProcess);
        server.createContext("/long-process", exchange -> redirect(exchange, "/redirect/1"));
        server.createContext("/redirect", this::longRedirect);
        server.createContext("/loop", exchange -> redirect(exchange, "/loop"));
        server.createContext("/untrusted", exchange -> redirect(exchange,
                "http://localhost:" + server.getAddress().getPort() + "/portal"));
        server.createContext("/scheme-change", exchange -> redirect(exchange,
                "https://127.0.0.1:" + server.getAddress().getPort() + "/portal"));
        server.createContext("/portal", exchange -> respond(exchange, 200, "portal-ok"));
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void 같은_세션에서_쿠키를_유지하고_POST_리다이렉트를_따른다() {
        // given
        SchoolSsoProperties properties = new SchoolSsoProperties(
                baseUri.resolve("/login"), baseUri.resolve("/process"),
                Duration.ofSeconds(2), Duration.ofSeconds(2), "bandi-test");
        SchoolSsoHttpSession session = new JdkSchoolSsoHttpSessionFactory(properties).create();

        // when
        SchoolSsoHttpResponse loginPage = session.get(properties.loginPageUrl());
        SchoolSsoHttpResponse portal = session.postForm(properties.loginProcessUrl(),
                properties.loginPageUrl(), Map.of("internalId", "encrypted-id", "internalPw", "encrypted-pw"));

        // then
        assertThat(loginPage.statusCode()).isEqualTo(200);
        assertThat(receivedCookie.get()).contains("KSESSIONID=session-1");
        assertThat(receivedForm.get()).contains("internalId=encrypted-id", "internalPw=encrypted-pw");
        assertThat(receivedReferer.get()).isEqualTo(properties.loginPageUrl().toString());
        assertThat(portal.statusCode()).isEqualTo(200);
        assertThat(portal.finalUri().getPath()).isEqualTo("/portal");
        assertThat(portal.body()).isEqualTo("portal-ok");
    }

    @Test
    void 여섯_번의_리다이렉트를_따라_최종_응답을_반환한다() {
        // given
        SchoolSsoProperties properties = properties("/long-process");
        SchoolSsoHttpSession session =
                new JdkSchoolSsoHttpSessionFactory(properties).create();

        // when
        SchoolSsoHttpResponse response = session.postForm(
                properties.loginProcessUrl(), properties.loginPageUrl(),
                Map.of("internalId", "encrypted-id", "internalPw", "encrypted-pw"));

        // then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.finalUri().getPath()).isEqualTo("/portal");
    }

    @Test
    void 허용_횟수를_넘는_리다이렉트는_응답_변경으로_차단한다() {
        // given
        SchoolSsoProperties properties = properties("/loop");
        SchoolSsoHttpSession session =
                new JdkSchoolSsoHttpSessionFactory(properties).create();

        // when & then
        assertThatThrownBy(() -> session.postForm(
                properties.loginProcessUrl(), properties.loginPageUrl(), Map.of()))
                .isInstanceOf(SchoolSsoResponseChangedException.class);
    }

    @Test
    void 허용되지_않은_호스트로의_리다이렉트는_응답_변경으로_차단한다() {
        // given
        SchoolSsoProperties properties = properties("/untrusted");
        SchoolSsoHttpSession session =
                new JdkSchoolSsoHttpSessionFactory(properties).create();

        // when & then
        assertThatThrownBy(() -> session.postForm(
                properties.loginProcessUrl(), properties.loginPageUrl(), Map.of()))
                .isInstanceOf(SchoolSsoResponseChangedException.class);
    }

    @Test
    void 프로토콜이_바뀌는_리다이렉트는_응답_변경으로_차단한다() {
        // given
        SchoolSsoProperties properties = properties("/scheme-change");
        SchoolSsoHttpSession session =
                new JdkSchoolSsoHttpSessionFactory(properties).create();

        // when & then
        assertThatThrownBy(() -> session.postForm(
                properties.loginProcessUrl(), properties.loginPageUrl(), Map.of()))
                .isInstanceOf(SchoolSsoResponseChangedException.class);
    }

    private void loginPage(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Set-Cookie", "KSESSIONID=session-1; Path=/; HttpOnly");
        respond(exchange, 200, "login-page");
    }

    private void loginProcess(HttpExchange exchange) throws IOException {
        receivedCookie.set(exchange.getRequestHeaders().getFirst("Cookie"));
        receivedReferer.set(exchange.getRequestHeaders().getFirst("Referer"));
        receivedForm.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        exchange.getResponseHeaders().add("Location", baseUri.resolve("/portal").toString());
        respond(exchange, 302, "redirect");
    }

    private void longRedirect(HttpExchange exchange) throws IOException {
        int index = Integer.parseInt(exchange.getRequestURI().getPath()
                .substring("/redirect/".length()));
        if (index < 5) {
            redirect(exchange, "/redirect/" + (index + 1));
            return;
        }
        redirect(exchange, "/portal");
    }

    private void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().add("Location", location);
        respond(exchange, 302, "redirect");
    }

    private SchoolSsoProperties properties(String processPath) {
        return new SchoolSsoProperties(
                baseUri.resolve("/login"), baseUri.resolve(processPath),
                Duration.ofSeconds(2), Duration.ofSeconds(2), "bandi-test");
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
