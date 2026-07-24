package kr.ac.tukorea.bandi.domain.member.sso.tukorea;

import kr.ac.tukorea.bandi.domain.member.exception.SchoolSsoResponseChangedException;
import kr.ac.tukorea.bandi.domain.member.model.AcademicStatus;
import kr.ac.tukorea.bandi.domain.member.model.SchoolIdentity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TukoreaSsoHtmlParserTest {

    private final TukoreaSsoHtmlParser parser = new TukoreaSsoHtmlParser();

    @Test
    void 로그인_페이지에서_세션별_AES_키를_추출한다() {
        String html = "<script>const keyHex = \"04033277038F3CE337E3AD59A8751A8E\";</script>";

        assertThat(parser.extractEncryptionKey(html))
                .isEqualTo("04033277038F3CE337E3AD59A8751A8E");
    }

    @Test
    void AES_키_패턴이_사라지면_학교_화면_변경으로_분류한다() {
        assertThatThrownBy(() -> parser.extractEncryptionKey("<html>changed</html>"))
                .isInstanceOf(SchoolSsoResponseChangedException.class);
    }

    @Test
    void 포털_HTML에서_필요한_신원만_추출한다() {
        String html = """
                <script>
                  var loginId = "2021184000";
                  var name = "김하늘";
                  var groupName = "재학생";
                </script>
                <div class="profile_02"><p>컴퓨터공학부(학생)</p></div>
                <div id="writeinfor">010-1234-5678 test@example.com</div>
                """;

        SchoolIdentity identity = parser.extractIdentity(html);

        assertThat(identity.studentNo()).isEqualTo("2021184000");
        assertThat(identity.name()).isEqualTo("김하늘");
        assertThat(identity.department()).isEqualTo("컴퓨터공학부");
        assertThat(identity.academicStatus()).isEqualTo(AcademicStatus.ENROLLED);
        assertThat(identity.phoneNumber()).isEqualTo("01012345678");
        assertThat(identity.toString()).doesNotContain("010-1234-5678", "test@example.com");
    }

    @Test
    void writeinfor의_형식에_맞지_않는_전화번호는_무시한다() {
        String html = """
                <script>
                  var loginId = "2021184000";
                  var name = "김하늘";
                  var groupName = "재학생";
                </script>
                <div class="profile_02"><p>컴퓨터공학부(학생)</p></div>
                <div id="writeinfor">연락처 미등록</div>
                """;

        SchoolIdentity identity = parser.extractIdentity(html);

        assertThat(identity.phoneNumber()).isNull();
    }

    @Test
    void 무관한_name_변수보다_프로필_환영_문구의_이름을_우선한다() {
        // given
        String html = """
                <script>var name = "메뉴";</script>
                <script>
                  var loginId = "2025591010";
                  var name = "다른 사용자";
                  var groupName = "재학생";
                </script>
                <h3>김현민 님 환영합니다!</h3>
                <div class="profile_02"><p>AI소프트웨어학과(학생)</p></div>
                """;

        // when
        SchoolIdentity identity = parser.extractIdentity(html);

        // then
        assertThat(identity.studentNo()).isEqualTo("2025591010");
        assertThat(identity.name()).isEqualTo("김현민");
    }

    @Test
    void 신원_script_블록_밖의_name_변수는_사용하지_않는다() {
        // given
        String html = """
                <script>var name = "메뉴";</script>
                <script>
                  var loginId = "2025591010";
                  var name = "김현민";
                  var groupName = "재학생";
                </script>
                <div class="profile_02"><p>AI소프트웨어학과(학생)</p></div>
                """;

        // when
        SchoolIdentity identity = parser.extractIdentity(html);

        // then
        assertThat(identity.name()).isEqualTo("김현민");
    }

    @Test
    void 필수_신원_필드가_사라지면_학교_화면_변경으로_분류한다() {
        String html = "<script>var loginId = \"2021184000\"; var name = \"김하늘\";</script>";

        assertThatThrownBy(() -> parser.extractIdentity(html))
                .isInstanceOf(SchoolSsoResponseChangedException.class);
    }
}
