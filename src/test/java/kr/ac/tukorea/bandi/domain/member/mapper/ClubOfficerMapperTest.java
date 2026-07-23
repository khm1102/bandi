package kr.ac.tukorea.bandi.domain.member.mapper;

import kr.ac.tukorea.bandi.domain.member.model.ClubOfficerPosition;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.domain.member.model.Team;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MapperTest
class ClubOfficerMapperTest {

    private final ClubOfficerMapper clubOfficerMapper;
    private final MemberMapper memberMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private Member president;

    @Autowired
    ClubOfficerMapperTest(ClubOfficerMapper clubOfficerMapper,
                          MemberMapper memberMapper, TeamMapper teamMapper,
                          CohortMapper cohortMapper) {
        this.clubOfficerMapper = clubOfficerMapper;
        this.memberMapper = memberMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
    }

    @BeforeEach
    void setUp() throws Exception {
        Team team = teamMapper.searchAll().get(0);
        Cohort cohort = new Cohort(null, "활동문서 테스트 기수", (short) 2026,
                CohortTerm.FIRST, true);
        cohortMapper.insert(cohort);
        president = new Member(null, "2026000001", "원동연", "공연예술학과",
                null, null, team.getTeamId(), cohort.getCohortId(),
                ClubRole.MEMBER, MemberStatus.ACTIVE, SsoLinkStatus.LINKED,
                null, null, null);
        memberMapper.insert(president);
        clubOfficerMapper.insert(ClubOfficerPosition.PRESIDENT,
                president.getMemberId(), LocalDateTime.of(2026, 7, 23, 20, 0));
    }

    @Test
    void 현재_활성_회장_이름을_조회한다() {
        assertThat(clubOfficerMapper.lookupActiveMemberNameByPosition(
                ClubOfficerPosition.PRESIDENT)).contains("원동연");
    }

    @Test
    void 회장이_비활성화되면_현재_회장으로_조회하지_않는다() {
        memberMapper.updateStatus(president.getMemberId(), MemberStatus.SUSPENDED);

        assertThat(clubOfficerMapper.lookupActiveMemberNameByPosition(
                ClubOfficerPosition.PRESIDENT)).isEmpty();
    }

    @Test
    void 회장_직책은_동시에_한_명만_배정할_수_있다() {
        assertThatThrownBy(() -> clubOfficerMapper.insert(
                ClubOfficerPosition.PRESIDENT, president.getMemberId(),
                LocalDateTime.of(2026, 7, 23, 21, 0)))
                .isInstanceOf(org.springframework.dao.DuplicateKeyException.class);
    }

    @Test
    void 회장_멤버를_변경하면_다음_조회부터_새_회장_이름이_반영된다() throws Exception {
        Team team = teamMapper.searchAll().get(0);
        Member nextPresident = new Member(null, "2026000002", "새회장", "디자인공학부",
                null, null, team.getTeamId(), president.getCohortId(),
                ClubRole.MEMBER, MemberStatus.ACTIVE, SsoLinkStatus.LINKED,
                null, null, null);
        memberMapper.insert(nextPresident);

        int updated = clubOfficerMapper.updateMember(ClubOfficerPosition.PRESIDENT,
                nextPresident.getMemberId(), LocalDateTime.of(2026, 7, 24, 10, 0));

        assertThat(updated).isOne();
        assertThat(clubOfficerMapper.lookupActiveMemberNameByPosition(
                ClubOfficerPosition.PRESIDENT)).contains("새회장");
    }
}
