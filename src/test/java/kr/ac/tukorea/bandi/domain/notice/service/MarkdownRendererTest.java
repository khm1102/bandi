package kr.ac.tukorea.bandi.domain.notice.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownRendererTest {

    private final MarkdownRenderer renderer = new MarkdownRenderer();

    @Test
    void GFM_표와_코드_링크를_안전한_HTML로_렌더링한다() {
        String markdown = """
                ## 안내

                | 항목 | 내용 |
                | --- | --- |
                | 장소 | 소극장 |

                `준비물`을 확인해 주세요. [동아리](https://bandi.example.com)
                """;

        String html = renderer.render(markdown).getValue();

        assertThat(html).contains("<h2>안내</h2>")
                .contains("<table>")
                .contains("<code>준비물</code>")
                .contains("href=\"https://bandi.example.com\"");
    }

    @Test
    void 원시_HTML과_스크립트_URL_HTTP_이미지를_제거한다() {
        String markdown = """
                <script>alert('xss')</script>
                [위험](javascript:alert(1))
                ![HTTP 이미지](http://example.com/image.png)
                ![HTTPS 이미지](https://example.com/image.png)
                <img src=x onerror=alert(1)>
                """;

        String html = renderer.render(markdown).getValue();

        assertThat(html).doesNotContain("script")
                .doesNotContain("javascript:")
                .doesNotContain("http://example.com/image.png")
                .doesNotContain("onerror")
                .contains("<img")
                .contains("https://example.com/image.png");
    }

    @Test
    void 첨부된_내부_이미지만_안전한_이미지_태그로_렌더링한다() {
        String markdown = "![공연 포스터](attachment://42)";

        String html = renderer.render(markdown, Map.of(42L,
                "/api/internal-notices/8/attachments/42/inline")).getValue();

        assertThat(html).contains("<img")
                .contains("/api/internal-notices/8/attachments/42/inline")
                .contains("alt=\"공연 포스터\"");
    }
}
