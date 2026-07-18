package kr.ac.tukorea.bandi.domain.member.client.tukorea;

import kr.ac.tukorea.bandi.domain.member.exception.SchoolSsoUnavailableException;
import kr.ac.tukorea.bandi.global.config.SchoolSsoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class JdkSchoolSsoHttpSessionFactory implements SchoolSsoHttpSessionFactory {

    private final SchoolSsoProperties properties;

    @Override
    public SchoolSsoHttpSession create() {
        CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        HttpClient httpClient = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .connectTimeout(properties.connectTimeout())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        return new JdkSchoolSsoHttpSession(httpClient, properties);
    }

    private static final class JdkSchoolSsoHttpSession implements SchoolSsoHttpSession {

        private final HttpClient httpClient;
        private final SchoolSsoProperties properties;

        private JdkSchoolSsoHttpSession(HttpClient httpClient, SchoolSsoProperties properties) {
            this.httpClient = httpClient;
            this.properties = properties;
        }

        @Override
        public SchoolSsoHttpResponse get(URI uri) {
            HttpRequest request = requestBuilder(uri).GET().build();
            return send(request);
        }

        @Override
        public SchoolSsoHttpResponse postForm(URI uri, URI referer, Map<String, String> form) {
            String body = form.entrySet().stream()
                    .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                    .collect(Collectors.joining("&"));
            HttpRequest request = requestBuilder(uri)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Referer", referer.toString())
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            return send(request);
        }

        private HttpRequest.Builder requestBuilder(URI uri) {
            return HttpRequest.newBuilder(uri)
                    .timeout(properties.requestTimeout())
                    .header("User-Agent", properties.userAgent());
        }

        private SchoolSsoHttpResponse send(HttpRequest request) {
            try {
                HttpResponse<String> response = httpClient.send(
                        request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                return new SchoolSsoHttpResponse(response.statusCode(), response.uri(), response.body());
            } catch (java.io.IOException exception) {
                throw new SchoolSsoUnavailableException();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new SchoolSsoUnavailableException();
            }
        }

        private String encode(String value) {
            return URLEncoder.encode(value, StandardCharsets.UTF_8);
        }
    }
}
