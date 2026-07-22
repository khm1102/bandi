package kr.ac.tukorea.bandi.domain.member.sso.tukorea;

import kr.ac.tukorea.bandi.domain.member.sso.SchoolCredentials;
import kr.ac.tukorea.bandi.domain.member.exception.SchoolCredentialsInvalidException;
import kr.ac.tukorea.bandi.domain.member.exception.SchoolSsoResponseChangedException;
import kr.ac.tukorea.bandi.domain.member.exception.SchoolSsoUnavailableException;
import kr.ac.tukorea.bandi.domain.member.model.AcademicStatus;
import kr.ac.tukorea.bandi.domain.member.model.SchoolIdentity;
import kr.ac.tukorea.bandi.global.config.SchoolSsoProperties;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TukoreaSchoolSsoClientTest {

    private static final URI LOGIN_PAGE = URI.create("https://ksc.tukorea.ac.kr/sso/login_stand.jsp");
    private static final URI LOGIN_PROCESS = URI.create(
            "https://ksc.tukorea.ac.kr/sso/login_proc.jsp?returnurl=null");
    private static final URI PORTAL = URI.create("https://portal.tukorea.ac.kr/portal/default/stu");
    private static final Clock CLOCK = Clock.fixed(Instant.ofEpochMilli(1721289600000L), ZoneOffset.UTC);

    @Test
    void 로그인_페이지_GET_후_암호화한_폼을_POST하고_신원을_반환한다() {
        // given
        FakeHttpSession session = new FakeHttpSession();
        session.enqueue(response(200, LOGIN_PAGE, loginPageHtml()));
        session.enqueue(response(200, PORTAL, portalHtml()));
        TukoreaSchoolSsoClient client = client(session);
        SchoolCredentials credentials = new SchoolCredentials("2021184000", "school-password");

        // when
        SchoolIdentity identity = client.authenticate(credentials);

        // then
        assertThat(session.operations).containsExactly("GET", "POST");
        assertThat(session.postedForm.get("internalId")).doesNotContain("2021184000");
        assertThat(session.postedForm.get("internalPw")).doesNotContain("school-password");
        assertThat(session.postedForm).containsEntry("gubun", "inter");
        assertThat(identity.studentNo()).isEqualTo("2021184000");
        assertThat(identity.academicStatus()).isEqualTo(AcademicStatus.ENROLLED);
    }

    @Test
    void 정상_포털의_무관한_alert는_로그인_실패로_판정하지_않는다() {
        FakeHttpSession session = new FakeHttpSession();
        session.enqueue(response(200, LOGIN_PAGE, loginPageHtml()));
        session.enqueue(response(200, PORTAL,
                portalHtml() + "<script>alert(\"포털 안내\");</script>"));

        SchoolIdentity identity = client(session).authenticate(credentials());

        assertThat(identity.studentNo()).isEqualTo("2021184000");
        assertThat(identity.academicStatus()).isEqualTo(AcademicStatus.ENROLLED);
    }

    @Test
    void 인증_실패_alert는_자격증명_오류로_분류한다() {
        FakeHttpSession session = new FakeHttpSession();
        session.enqueue(response(200, LOGIN_PAGE, loginPageHtml()));
        session.enqueue(response(200, LOGIN_PROCESS,
                "<script>alert(\"인증에 실패했습니다.\"); top.location.href = \"/sso/login_stand.jsp\";</script>"));

        assertThatThrownBy(() -> client(session).authenticate(credentials()))
                .isInstanceOf(SchoolCredentialsInvalidException.class);
    }

    @Test
    void 알_수_없는_alert와_로그인_페이지_반송은_응답_구조_변경으로_분류한다() {
        FakeHttpSession session = new FakeHttpSession();
        session.enqueue(response(200, LOGIN_PAGE, loginPageHtml()));
        session.enqueue(response(200, LOGIN_PROCESS,
                "<script>alert(\"알 수 없는 오류\"); "
                        + "top.location.href = \"/sso/login_stand.jsp\";</script>"));

        assertThatThrownBy(() -> client(session).authenticate(credentials()))
                .isInstanceOf(SchoolSsoResponseChangedException.class);
    }

    @Test
    void 학교_서버_5xx는_일시적_장애로_분류한다() {
        FakeHttpSession session = new FakeHttpSession();
        session.enqueue(response(503, LOGIN_PAGE, "maintenance"));

        assertThatThrownBy(() -> client(session).authenticate(credentials()))
                .isInstanceOf(SchoolSsoUnavailableException.class);
    }

    @Test
    void 포털이_아닌_곳에_착지하면_응답_구조_변경으로_분류한다() {
        FakeHttpSession session = new FakeHttpSession();
        session.enqueue(response(200, LOGIN_PAGE, loginPageHtml()));
        session.enqueue(response(200, URI.create("https://ksc.tukorea.ac.kr/unknown"), "<html>unknown</html>"));

        assertThatThrownBy(() -> client(session).authenticate(credentials()))
                .isInstanceOf(SchoolSsoResponseChangedException.class);
    }

    @Test
    void 포털_필수_필드가_사라지면_응답_구조_변경으로_분류한다() {
        FakeHttpSession session = new FakeHttpSession();
        session.enqueue(response(200, LOGIN_PAGE, loginPageHtml()));
        session.enqueue(response(200, PORTAL, "<script>var loginId = \"2021184000\";</script>"));

        assertThatThrownBy(() -> client(session).authenticate(credentials()))
                .isInstanceOf(SchoolSsoResponseChangedException.class);
    }

    private TukoreaSchoolSsoClient client(FakeHttpSession session) {
        SchoolSsoProperties properties = new SchoolSsoProperties(
                LOGIN_PAGE, LOGIN_PROCESS, Duration.ofSeconds(5), Duration.ofSeconds(15), "bandi-test");
        return new TukoreaSchoolSsoClient(() -> session, new TukoreaSsoHtmlParser(),
                new AesCredentialEncryptor(() -> new byte[16]), CLOCK, properties);
    }

    private SchoolCredentials credentials() {
        return new SchoolCredentials("2021184000", "school-password");
    }

    private SchoolSsoHttpResponse response(int status, URI uri, String body) {
        return new SchoolSsoHttpResponse(status, uri, body);
    }

    private String loginPageHtml() {
        return "<script>const keyHex = \"000102030405060708090A0B0C0D0E0F\";</script>";
    }

    private String portalHtml() {
        return """
                <script>
                  var loginId = "2021184000";
                  var name = "김하늘";
                  var groupName = "재학생";
                </script>
                <div class="profile_02"><p>컴퓨터공학부(학생)</p></div>
                """;
    }

    private static final class FakeHttpSession implements SchoolSsoHttpSession {

        private final Deque<SchoolSsoHttpResponse> responses = new ArrayDeque<>();
        private final List<String> operations = new ArrayList<>();
        private Map<String, String> postedForm;

        void enqueue(SchoolSsoHttpResponse response) {
            responses.add(response);
        }

        @Override
        public SchoolSsoHttpResponse get(URI uri) {
            operations.add("GET");
            return responses.remove();
        }

        @Override
        public SchoolSsoHttpResponse postForm(URI uri, URI referer, Map<String, String> form) {
            operations.add("POST");
            postedForm = form;
            return responses.remove();
        }
    }
}
