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
        assertThat(identity.toString()).doesNotContain("010-1234-5678", "test@example.com");
    }

    @Test
    void 필수_신원_필드가_사라지면_학교_화면_변경으로_분류한다() {
        String html = "<script>var loginId = \"2021184000\"; var name = \"김하늘\";</script>";

        assertThatThrownBy(() -> parser.extractIdentity(html))
                .isInstanceOf(SchoolSsoResponseChangedException.class);
    }
}
