package kr.ac.tukorea.bandi.domain.member.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberAccessContextTest {

    @Test
    void 활성_ADMIN은_전체와_모든_팀을_관리할_수_있다() {
        MemberAccessContext context = new MemberAccessContext(1L, 4L, true, false, true);

        assertThat(context.canReadInternal()).isTrue();
        assertThat(context.canManageGlobal()).isTrue();
        assertThat(context.canManageTeam(5L)).isTrue();
    }

    @Test
    void 활성_LEADER는_본인_팀만_관리할_수_있다() {
        MemberAccessContext context = new MemberAccessContext(2L, 4L, false, true, true);

        assertThat(context.canReadInternal()).isTrue();
        assertThat(context.canManageGlobal()).isFalse();
        assertThat(context.canManageTeam(4L)).isTrue();
        assertThat(context.canManageTeam(5L)).isFalse();
        assertThat(context.canManageTeam(null)).isFalse();
    }

    @Test
    void 활성_MEMBER는_내부_조회만_가능하다() {
        MemberAccessContext context = new MemberAccessContext(3L, 4L, false, false, true);

        assertThat(context.canReadInternal()).isTrue();
        assertThat(context.canManageGlobal()).isFalse();
        assertThat(context.canManageTeam(4L)).isFalse();
        assertThat(context.canContributeToTeam(4L)).isTrue();
        assertThat(context.canContributeToTeam(5L)).isFalse();
        assertThat(context.canChangeOwnTeam(3L)).isTrue();
        assertThat(context.canChangeOwnTeam(4L)).isFalse();
    }

    @Test
    void 비활성_멤버는_내부_조회와_관리를_할_수_없다() {
        MemberAccessContext context = new MemberAccessContext(4L, 4L, true, false, false);

        assertThat(context.canReadInternal()).isFalse();
        assertThat(context.canManageGlobal()).isFalse();
        assertThat(context.canManageTeam(4L)).isFalse();
        assertThat(context.canContributeToTeam(4L)).isFalse();
        assertThat(context.canChangeOwnTeam(4L)).isFalse();
    }
}
