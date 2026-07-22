package kr.ac.tukorea.bandi.domain.member.sso.tukorea;

import kr.ac.tukorea.bandi.global.config.SchoolSsoProperties;
import kr.ac.tukorea.bandi.domain.member.exception.SchoolSsoResponseChangedException;
import kr.ac.tukorea.bandi.domain.member.exception.SchoolSsoUnavailableException;
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
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class JdkSchoolSsoHttpSessionFactory implements SchoolSsoHttpSessionFactory {

    private static final URI PORTAL_ORIGIN = URI.create("https://portal.tukorea.ac.kr");
    private static final int MAX_REDIRECTS = 10;

    private final SchoolSsoProperties properties;

    @Override
    public SchoolSsoHttpSession create() {
        CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        HttpClient httpClient = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .connectTimeout(properties.connectTimeout())
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        return new JdkSchoolSsoHttpSession(httpClient, properties);
    }

    private static final class JdkSchoolSsoHttpSession implements SchoolSsoHttpSession {

        private final HttpClient httpClient;
        private final SchoolSsoProperties properties;
        private final Set<String> allowedOrigins;

        private JdkSchoolSsoHttpSession(HttpClient httpClient, SchoolSsoProperties properties) {
            this.httpClient = httpClient;
            this.properties = properties;
            this.allowedOrigins = allowedOrigins(properties);
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
            HttpRequest currentRequest = request;
            for (int redirectCount = 0; ; redirectCount++) {
                HttpResponse<String> response = sendOnce(currentRequest);
                if (!isRedirect(response.statusCode())) {
                    return toSchoolResponse(response);
                }
                if (redirectCount >= MAX_REDIRECTS) {
                    throw new SchoolSsoResponseChangedException();
                }
                URI target = resolveRedirectTarget(response);
                currentRequest = createRedirectRequest(
                        currentRequest, response.statusCode(), target);
            }
        }

        private HttpResponse<String> sendOnce(HttpRequest request) {
            try {
                return httpClient.send(
                        request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            } catch (java.io.IOException exception) {
                throw new SchoolSsoUnavailableException();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new SchoolSsoUnavailableException();
            }
        }

        private boolean isRedirect(int statusCode) {
            return statusCode == 301 || statusCode == 302 || statusCode == 303
                    || statusCode == 307 || statusCode == 308;
        }

        private URI resolveRedirectTarget(HttpResponse<String> response) {
            String location = response.headers().firstValue("Location")
                    .orElseThrow(SchoolSsoResponseChangedException::new);
            URI target;
            try {
                target = response.uri().resolve(location);
            } catch (IllegalArgumentException exception) {
                throw new SchoolSsoResponseChangedException();
            }
            if (!sameScheme(response.uri(), target)
                    || !allowedOrigins.contains(origin(target))) {
                throw new SchoolSsoResponseChangedException();
            }
            return target;
        }

        private HttpRequest createRedirectRequest(
                HttpRequest previousRequest,
                int statusCode,
                URI target
        ) {
            HttpRequest.Builder builder = requestBuilder(target)
                    .header("Referer", previousRequest.uri().toString());
            if (statusCode != 307 && statusCode != 308) {
                return builder.GET().build();
            }
            if (!origin(previousRequest.uri()).equals(origin(target))) {
                throw new SchoolSsoResponseChangedException();
            }
            previousRequest.headers().firstValue("Content-Type")
                    .ifPresent(contentType -> builder.header("Content-Type", contentType));
            return builder.method(previousRequest.method(), previousRequest.bodyPublisher()
                    .orElse(HttpRequest.BodyPublishers.noBody())).build();
        }

        private SchoolSsoHttpResponse toSchoolResponse(HttpResponse<String> response) {
            return new SchoolSsoHttpResponse(
                    response.statusCode(), response.uri(), response.body());
        }

        private boolean sameScheme(URI source, URI target) {
            return source.getScheme() != null && target.getScheme() != null
                    && source.getScheme().equalsIgnoreCase(target.getScheme());
        }

        private Set<String> allowedOrigins(SchoolSsoProperties properties) {
            Set<String> origins = new HashSet<>();
            origins.add(origin(properties.loginPageUrl()));
            origins.add(origin(properties.loginProcessUrl()));
            origins.add(origin(PORTAL_ORIGIN));
            return Set.copyOf(origins);
        }

        private String origin(URI uri) {
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new SchoolSsoResponseChangedException();
            }
            int port = uri.getPort();
            if (port < 0) {
                port = "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
            }
            return uri.getScheme().toLowerCase(Locale.ROOT) + "://"
                    + uri.getHost().toLowerCase(Locale.ROOT) + ":" + port;
        }

        private String encode(String value) {
            return URLEncoder.encode(value, StandardCharsets.UTF_8);
        }
    }
}
