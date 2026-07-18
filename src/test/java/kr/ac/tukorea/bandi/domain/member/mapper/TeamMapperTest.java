package kr.ac.tukorea.bandi.domain.member.mapper;

import kr.ac.tukorea.bandi.domain.member.model.Team;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MapperTest
class TeamMapperTest {

    private final TeamMapper teamMapper;

    @Autowired
    TeamMapperTest(TeamMapper teamMapper) {
        this.teamMapper = teamMapper;
    }

    @Test
    void 초기_팀_기준_데이터_11개가_존재한다() {
        // when
        List<Team> teams = teamMapper.searchAll();

        // then — 정본 5.1의 명단을 통합·개명 없이 그대로 등록한다
        assertThat(teams).hasSize(11);
        assertThat(teams).extracting(Team::getName)
                .containsExactly("연출", "조연출", "배우", "무대팀", "오퍼팀", "디자인팀",
                        "영상팀", "영상 배우", "영상 촬영", "영상 연출", "영상 편집");
    }

    @Test
    void 팀을_저장하고_단건_조회한다() {
        // given
        Team team = new Team(null, "홍보팀", 12, true);

        // when
        teamMapper.insert(team);
        Optional<Team> found = teamMapper.lookupById(team.getTeamId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("홍보팀");
        assertThat(found.get().getDisplayOrder()).isEqualTo(12);
        assertThat(found.get().isActive()).isTrue();
    }

    @Test
    void 같은_팀명은_저장할_수_없다() {
        // given — uk_team_name
        Team duplicated = new Team(null, "배우", 99, true);

        // when & then
        assertThatThrownBy(() -> teamMapper.insert(duplicated))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void 팀을_비활성화해도_행은_남는다() {
        // given
        Team team = new Team(null, "홍보팀", 12, true);
        teamMapper.insert(team);

        // when — 팀은 삭제하지 않고 비활성화한다 (정본 5.1)
        teamMapper.updateActive(team.getTeamId(), false);

        // then
        assertThat(teamMapper.lookupById(team.getTeamId()))
                .isPresent()
                .get()
                .extracting(Team::isActive)
                .isEqualTo(false);
    }
}
