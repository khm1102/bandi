package kr.ac.tukorea.bandi.domain.member.model;

import kr.ac.tukorea.bandi.domain.member.exception.InactiveTeamException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeamTest {

    @Test
    void 활성_팀에는_멤버를_배정할_수_있다() {
        // given
        Team team = new Team(1L, "배우", 3, true);

        // when & then
        assertThatCode(team::validateAssignable).doesNotThrowAnyException();
    }

    @Test
    void 비활성_팀에는_멤버를_배정할_수_없다() {
        // given — 팀은 기록 보존을 위해 삭제하지 않고 비활성화한다 (정본 5.1)
        Team team = new Team(1L, "배우", 3, false);

        // when & then
        assertThatThrownBy(team::validateAssignable)
                .isInstanceOf(InactiveTeamException.class);
    }
}
