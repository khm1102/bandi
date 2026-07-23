package kr.ac.tukorea.bandi.domain.resource.model;

import kr.ac.tukorea.bandi.domain.resource.exception.InvalidResourceException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceTest {

    @Test
    void 작성자는_제목과_Markdown_본문으로_자료를_만든다() {
        Resource resource = Resource.create("공연 대본", "# 1막", 1L);

        assertThat(resource.getTitle()).isEqualTo("공연 대본");
        assertThat(resource.getBodyMarkdown()).isEqualTo("# 1막");
        assertThat(resource.isCreatedBy(1L)).isTrue();
    }

    @Test
    void 수정해도_원_작성자는_유지하고_수정자만_바뀐다() {
        Resource resource = new Resource(10L, "초안", "내용", 1L, 1L,
                null, null, null);

        Resource edited = resource.edit("수정본", "수정 내용", 2L);

        assertThat(edited.getCreatedByMemberId()).isEqualTo(1L);
        assertThat(edited.getUpdatedByMemberId()).isEqualTo(2L);
        assertThat(edited.getTitle()).isEqualTo("수정본");
    }

    @Test
    void 빈_본문과_200자를_넘는_제목은_거부한다() {
        assertThatThrownBy(() -> Resource.create("제목", " ", 1L))
                .isInstanceOf(InvalidResourceException.class);
        assertThatThrownBy(() -> Resource.create("가".repeat(201), "본문", 1L))
                .isInstanceOf(InvalidResourceException.class);
    }
}
