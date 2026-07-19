package kr.ac.tukorea.bandi.domain.resource.model;

import kr.ac.tukorea.bandi.domain.resource.exception.InvalidResourceException;
import kr.ac.tukorea.bandi.domain.resource.exception.InvalidResourceStateException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long TEAM_ID = 4L;

    @Test
    void 전체_자료는_팀이_없고_팀_자료는_팀이_필수다() {
        Resource all = Resource.draft(ResourceTargetScope.ALL, null,
                "SCRIPT", "전체 대본", "최종 대본", true, ACTOR_ID);
        Resource team = Resource.draft(ResourceTargetScope.TEAM, TEAM_ID,
                "MINUTES", "팀 회의록", "회의 기록", false, ACTOR_ID);

        assertThat(all.getTeamId()).isNull();
        assertThat(team.getTeamId()).isEqualTo(TEAM_ID);
    }

    @Test
    void 대상_범위와_팀이_맞지_않으면_거부한다() {
        assertThatThrownBy(() -> Resource.draft(ResourceTargetScope.ALL, TEAM_ID,
                "SCRIPT", "제목", "설명", false, ACTOR_ID))
                .isInstanceOf(InvalidResourceException.class);
        assertThatThrownBy(() -> Resource.draft(ResourceTargetScope.TEAM, null,
                "SCRIPT", "제목", "설명", false, ACTOR_ID))
                .isInstanceOf(InvalidResourceException.class);
    }

    @Test
    void 카테고리와_제목과_설명은_필수이며_길이를_제한한다() {
        assertThatThrownBy(() -> Resource.draft(ResourceTargetScope.ALL, null,
                " ", "제목", "설명", false, ACTOR_ID))
                .isInstanceOf(InvalidResourceException.class);
        assertThatThrownBy(() -> Resource.draft(ResourceTargetScope.ALL, null,
                "SCRIPT", "가".repeat(201), "설명", false, ACTOR_ID))
                .isInstanceOf(InvalidResourceException.class);
        assertThatThrownBy(() -> Resource.draft(ResourceTargetScope.ALL, null,
                "SCRIPT", "제목", " ", false, ACTOR_ID))
                .isInstanceOf(InvalidResourceException.class);
    }

    @Test
    void 초안을_게시하면_읽을_수_있다() {
        Resource draft = draft();

        Resource published = draft.publish(ACTOR_ID);

        assertThat(published.getStatus()).isEqualTo(ResourceStatus.PUBLISHED);
        assertThat(published.isReadable()).isTrue();
    }

    @Test
    void 게시된_자료는_메타데이터를_수정해도_게시_상태를_유지한다() {
        Resource published = draft().publish(ACTOR_ID);

        Resource changed = published.edit(ResourceTargetScope.TEAM, TEAM_ID,
                "VIDEO", "수정 제목", "수정 설명", false, 2L);

        assertThat(changed.getStatus()).isEqualTo(ResourceStatus.PUBLISHED);
        assertThat(changed.getUpdatedByMemberId()).isEqualTo(2L);
        assertThat(changed.getTeamId()).isEqualTo(TEAM_ID);
    }

    @Test
    void 보관하면_읽거나_수정하거나_게시할_수_없다() {
        Resource archived = draft().archive(ACTOR_ID);

        assertThat(archived.isReadable()).isFalse();
        assertThatThrownBy(() -> archived.edit(ResourceTargetScope.ALL, null,
                "SCRIPT", "수정", "설명", false, ACTOR_ID))
                .isInstanceOf(InvalidResourceStateException.class);
        assertThatThrownBy(() -> archived.publish(ACTOR_ID))
                .isInstanceOf(InvalidResourceStateException.class);
    }

    @Test
    void 게시된_자료를_다시_게시할_수_없다() {
        Resource published = draft().publish(ACTOR_ID);

        assertThatThrownBy(() -> published.publish(ACTOR_ID))
                .isInstanceOf(InvalidResourceStateException.class);
    }

    private Resource draft() {
        return Resource.draft(ResourceTargetScope.ALL, null, "SCRIPT",
                "공연 대본", "최종 대본", true, ACTOR_ID);
    }
}
