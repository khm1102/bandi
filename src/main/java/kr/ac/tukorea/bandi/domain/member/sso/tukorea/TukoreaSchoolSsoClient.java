package kr.ac.tukorea.bandi.domain.member.sso.tukorea;

import kr.ac.tukorea.bandi.domain.member.sso.SchoolCredentials;
import kr.ac.tukorea.bandi.domain.member.sso.SchoolSsoClient;
import kr.ac.tukorea.bandi.domain.member.exception.SchoolCredentialsInvalidException;
import kr.ac.tukorea.bandi.domain.member.exception.SchoolSsoResponseChangedException;
import kr.ac.tukorea.bandi.domain.member.exception.SchoolSsoUnavailableException;
import kr.ac.tukorea.bandi.domain.member.model.SchoolIdentity;
import kr.ac.tukorea.bandi.global.config.SchoolSsoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TukoreaSchoolSsoClient implements SchoolSsoClient {

    private static final String PORTAL_HOST = "portal.tukorea.ac.kr";

    private final SchoolSsoHttpSessionFactory httpSessionFactory;
    private final TukoreaSsoHtmlParser htmlParser;
    private final AesCredentialEncryptor credentialEncryptor;
    private final Clock clock;
    private final SchoolSsoProperties properties;

    @Override
    public SchoolIdentity authenticate(SchoolCredentials credentials) {
        SchoolSsoHttpSession session = httpSessionFactory.create();
        SchoolSsoHttpResponse loginPage = session.get(properties.loginPageUrl());
        validateResponseStatus(loginPage);
        String keyHex = htmlParser.extractEncryptionKey(loginPage.body());

        long timestampMillis = clock.millis();
        Map<String, String> form = createLoginForm(credentials, timestampMillis, keyHex);
        SchoolSsoHttpResponse portalResponse = session.postForm(
                properties.loginProcessUrl(), properties.loginPageUrl(), form);
        validateResponseStatus(portalResponse);
        validateAuthenticationResponse(portalResponse);
        return htmlParser.extractIdentity(portalResponse.body());
    }

    private Map<String, String> createLoginForm(
            SchoolCredentials credentials,
            long timestampMillis,
            String keyHex
    ) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("internalId", credentialEncryptor.encrypt(credentials.studentNo(), timestampMillis, keyHex));
        form.put("internalPw", credentialEncryptor.encrypt(credentials.password(), timestampMillis, keyHex));
        form.put("externalId", "");
        form.put("externalPw", "");
        form.put("gubun", "inter");
        return Map.copyOf(form);
    }

    private void validateResponseStatus(SchoolSsoHttpResponse response) {
        if (response.statusCode() >= 500) {
            throw new SchoolSsoUnavailableException();
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new SchoolSsoResponseChangedException();
        }
    }

    private void validateAuthenticationResponse(SchoolSsoHttpResponse response) {
        if (htmlParser.looksLikeLoginPage(response.body())) {
            boolean invalidCredentials = htmlParser.extractAlert(response.body())
                    .filter(alert -> alert.contains("인증에 실패"))
                    .isPresent();
            if (invalidCredentials) {
                throw new SchoolCredentialsInvalidException();
            }
            throw new SchoolSsoResponseChangedException();
        }
        String host = response.finalUri() == null ? "" : response.finalUri().getHost();
        if (!PORTAL_HOST.equalsIgnoreCase(host)) {
            throw new SchoolSsoResponseChangedException();
        }
    }
}
