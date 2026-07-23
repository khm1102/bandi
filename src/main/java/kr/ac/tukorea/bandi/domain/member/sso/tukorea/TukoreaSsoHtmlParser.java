package kr.ac.tukorea.bandi.domain.member.sso.tukorea;

import kr.ac.tukorea.bandi.domain.member.exception.SchoolSsoResponseChangedException;
import kr.ac.tukorea.bandi.domain.member.model.AcademicStatus;
import kr.ac.tukorea.bandi.domain.member.model.SchoolIdentity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
class TukoreaSsoHtmlParser {

    private static final Pattern KEY = Pattern.compile("keyHex\\s*=\\s*\"([0-9A-Fa-f]{32})\"");
    private static final Pattern ALERT = Pattern.compile("alert\\(\\s*[\"']([^\"']*)[\"']\\s*\\)");
    private static final Pattern STUDENT_NO = Pattern.compile("var\\s+loginId\\s*=\\s*\"(\\d+)\"");
    private static final Pattern NAME = Pattern.compile("var\\s+name\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern PROFILE_NAME = Pattern.compile("<h3[^>]*>\\s*([가-힣]{2,20})\\s*님");
    private static final Pattern ACADEMIC_STATUS = Pattern.compile("var\\s+groupName\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern DEPARTMENT = Pattern.compile(
            "class=\"profile_02\"[^>]*>\\s*<p>\\s*([^<]+?)\\s*</p>", Pattern.DOTALL);
    private static final Pattern SCRIPT = Pattern.compile(
            "<script\\b[^>]*>(.*?)</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    String extractEncryptionKey(String html) {
        return find(KEY, html).orElseThrow(SchoolSsoResponseChangedException::new);
    }

    Optional<String> extractAlert(String html) {
        return find(ALERT, html);
    }

    boolean looksLikeLoginPage(String html) {
        return html.contains("id=\"loginForm\"")
                || html.contains("login_stand.jsp")
                || html.contains("internalPw");
    }

    SchoolIdentity extractIdentity(String html) {
        String studentNo = find(STUDENT_NO, html).orElseThrow(SchoolSsoResponseChangedException::new);
        String name = find(PROFILE_NAME, html)
                .or(() -> findNameInIdentityScript(html))
                .orElseThrow(SchoolSsoResponseChangedException::new);
        String academicLabel = find(ACADEMIC_STATUS, html)
                .orElseThrow(SchoolSsoResponseChangedException::new);
        String department = find(DEPARTMENT, html)
                .map(this::normalizeDepartment)
                .orElse(null);
        return new SchoolIdentity(studentNo, name, department, AcademicStatus.fromPortalLabel(academicLabel));
    }

    private Optional<String> findNameInIdentityScript(String html) {
        Matcher scriptMatcher = SCRIPT.matcher(html == null ? "" : html);
        while (scriptMatcher.find()) {
            String script = scriptMatcher.group(1);
            if (find(STUDENT_NO, script).isEmpty() || find(ACADEMIC_STATUS, script).isEmpty()) {
                continue;
            }
            return find(NAME, script);
        }
        return Optional.empty();
    }

    private Optional<String> find(Pattern pattern, String html) {
        Matcher matcher = pattern.matcher(html == null ? "" : html);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(matcher.group(1).trim());
    }

    private String normalizeDepartment(String rawDepartment) {
        return rawDepartment.replaceFirst("\\s*\\([^)]*\\)\\s*$", "").trim();
    }
}
