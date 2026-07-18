package kr.ac.tukorea.bandi.domain.member.mapper;

import kr.ac.tukorea.bandi.domain.member.dto.request.MemberSearchCondition;
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
import org.springframework.dao.DuplicateKeyException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MapperTest
class MemberMapperTest {

    private final MemberMapper memberMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final DataSource dataSource;

    private Long actorTeamId;
    private Long stageTeamId;
    private Long cohortId;
    private Long newCohortId;

    @Autowired
    MemberMapperTest(MemberMapper memberMapper, TeamMapper teamMapper,
                     CohortMapper cohortMapper, DataSource dataSource) {
        this.memberMapper = memberMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.dataSource = dataSource;
    }

    @BeforeEach
    void setUp() {
        List<Team> teams = teamMapper.searchAll();
        actorTeamId = teams.get(2).getTeamId();
        stageTeamId = teams.get(3).getTeamId();

        Cohort cohort = new Cohort(null, "26-2기", (short) 2026, CohortTerm.SECOND, true);
        cohortMapper.insert(cohort);
        cohortId = cohort.getCohortId();

        Cohort newCohort = new Cohort(null, "27-1기", (short) 2027, CohortTerm.FIRST, true);
        cohortMapper.insert(newCohort);
        newCohortId = newCohort.getCohortId();
    }

    private Member preRegistered(String studentNo, Long teamId) {
        return Member.preRegister(studentNo, "김하늘", teamId, cohortId, ClubRole.MEMBER, null);
    }

    @Test
    void 멤버를_저장하고_학번으로_조회한다() {
        // given
        Member member = preRegistered("2021184000", actorTeamId);

        // when
        memberMapper.insert(member);
        Optional<Member> found = memberMapper.lookupByStudentNo("2021184000");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(MemberStatus.PRE_REGISTERED);
        assertThat(found.get().getSsoLinkStatus()).isEqualTo(SsoLinkStatus.WAITING);
        assertThat(found.get().getRole()).isEqualTo(ClubRole.MEMBER);
        assertThat(found.get().getTeamId()).isEqualTo(actorTeamId);
    }

    @Test
    void 저장하면_생성된_식별자가_채워진다() {
        // given
        Member member = preRegistered("2021184000", actorTeamId);

        // when
        memberMapper.insert(member);

        // then — useGeneratedKeys가 setter 없는 model에도 동작해야 한다
        assertThat(member.getMemberId()).isNotNull();
    }

    @Test
    void 같은_학번은_저장할_수_없다() {
        // given — uk_member_student_no
        memberMapper.insert(preRegistered("2021184000", actorTeamId));

        // when & then
        assertThatThrownBy(() -> memberMapper.insert(preRegistered("2021184000", stageTeamId)))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void 학번_존재_여부를_확인한다() {
        // given
        memberMapper.insert(preRegistered("2021184000", actorTeamId));

        // when & then
        assertThat(memberMapper.existsByStudentNo("2021184000")).isTrue();
        assertThat(memberMapper.existsByStudentNo("2021184999")).isFalse();
    }

    @Test
    void 팀과_상태로_멤버를_검색한다() {
        // given
        memberMapper.insert(preRegistered("2021184000", actorTeamId));
        memberMapper.insert(preRegistered("2021184001", stageTeamId));

        // when
        List<Member> found = memberMapper.searchByCondition(
                new MemberSearchCondition(actorTeamId, MemberStatus.PRE_REGISTERED, null));

        // then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getStudentNo()).isEqualTo("2021184000");
    }

    @Test
    void 팀을_변경하면_현재_팀이_갱신된다() {
        // given
        Member member = preRegistered("2021184000", actorTeamId);
        memberMapper.insert(member);

        // when
        memberMapper.updateTeam(member.getMemberId(), stageTeamId);

        // then
        assertThat(memberMapper.lookupById(member.getMemberId()))
                .isPresent()
                .get()
                .extracting(Member::getTeamId)
                .isEqualTo(stageTeamId);
    }

    @Test
    void 기수를_변경하면_현재_기수가_갱신된다() {
        // given
        Member member = preRegistered("2021184000", actorTeamId);
        memberMapper.insert(member);

        // when
        memberMapper.updateCohort(member.getMemberId(), newCohortId);

        // then
        assertThat(memberMapper.lookupById(member.getMemberId()))
                .isPresent()
                .get()
                .extracting(Member::getCohortId)
                .isEqualTo(newCohortId);
    }

    @Test
    void 활성_ADMIN만_잠금_조회된다() {
        // given — 활성 ADMIN 한 명, 사전 등록 ADMIN 한 명, 활성 일반 부원 한 명
        Member activeAdmin = new Member(null, "2020184000", "이서준", null, null, null,
                actorTeamId, cohortId, ClubRole.ADMIN, MemberStatus.ACTIVE, SsoLinkStatus.LINKED,
                null, null, null);
        memberMapper.insert(activeAdmin);
        memberMapper.insert(Member.preRegister("2020184001", "박서연", actorTeamId, cohortId,
                ClubRole.ADMIN, null));
        memberMapper.insert(new Member(null, "2020184002", "정도윤", null, null, null,
                actorTeamId, cohortId, ClubRole.MEMBER, MemberStatus.ACTIVE, SsoLinkStatus.LINKED,
                null, null, null));

        // when
        List<Long> activeAdminIds = memberMapper.searchActiveAdminIdsForUpdate();

        // then
        assertThat(activeAdminIds).containsExactly(activeAdmin.getMemberId());
    }

    @Test
    void 허용되지_않는_role_code는_CHECK_제약으로_거부된다() throws SQLException {
        // given — Java enum을 우회한 직접 INSERT로 ck_member_role_code를 검증한다
        String sql = """
                INSERT INTO member (student_no, name, team_id, cohort_id, role_code,
                                    member_status_code, sso_link_status_code)
                VALUES ('2021184777', '테스트', %d, %d, 'PRESIDENT', 'ACTIVE', 'LINKED')
                """.formatted(actorTeamId, cohortId);

        // when & then
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            assertThatThrownBy(() -> statement.executeUpdate(sql))
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("ck_member_role_code");
        }
    }
}
