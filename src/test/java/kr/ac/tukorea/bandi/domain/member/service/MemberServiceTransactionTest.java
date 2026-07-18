package kr.ac.tukorea.bandi.domain.member.service;

import kr.ac.tukorea.bandi.domain.member.dto.request.CohortChangeParam;
import kr.ac.tukorea.bandi.domain.member.dto.request.StatusChangeParam;
import kr.ac.tukorea.bandi.domain.member.dto.request.TeamChangeParam;
import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberHistoryMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.Team;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;

/**
 * 현재값 갱신과 이력 삽입이 실제로 한 트랜잭션으로 묶이는지 검증한다 (정본 5.4).
 *
 * <p>Mockito 단위 테스트로는 롤백을 확인할 수 없으므로 실제 DB와 트랜잭션을 사용한다.
 * 클래스에 {@code @Transactional}을 붙이지 않는다 — 테스트가 바깥 트랜잭션을 열면
 * Service의 롤백이 가려져 검증 자체가 무의미해진다. 대신 만든 데이터를 직접 정리한다.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("멤버 변경 트랜잭션")
class MemberServiceTransactionTest {

    private final MemberService memberService;
    private final MemberMapper memberMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final DataSource dataSource;

    @MockitoBean
    private MemberHistoryMapper memberHistoryMapper;

    private Long actorTeamId;
    private Long stageTeamId;
    private Long memberId;
    private Long adminId;
    private Long cohortId;
    private Long newCohortId;

    @Autowired
    MemberServiceTransactionTest(MemberService memberService, MemberMapper memberMapper,
                                 TeamMapper teamMapper, CohortMapper cohortMapper, DataSource dataSource) {
        this.memberService = memberService;
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

        Cohort cohort = new Cohort(null, "트랜잭션검증기수", (short) 2999, "FIRST", true);
        cohortMapper.insert(cohort);
        cohortId = cohort.getCohortId();

        Cohort newCohort = new Cohort(null, "트랜잭션검증새기수", (short) 2998, "SECOND", true);
        cohortMapper.insert(newCohort);
        newCohortId = newCohort.getCohortId();

        Member admin = Member.preRegister("2999184001", "이서준", actorTeamId, cohortId,
                ClubRole.ADMIN, null);
        memberMapper.insert(admin);
        adminId = admin.getMemberId();
        memberMapper.updateStatus(adminId, MemberStatus.ACTIVE);

        Member member = Member.preRegister("2999184000", "김하늘", actorTeamId, cohortId,
                ClubRole.MEMBER, adminId);
        memberMapper.insert(member);
        memberId = member.getMemberId();
    }

    @AfterEach
    void tearDown() throws SQLException {
        executeUpdate("DELETE FROM member_team_history WHERE member_id = " + memberId);
        executeUpdate("DELETE FROM member WHERE member_id = " + memberId);
        executeUpdate("DELETE FROM member WHERE member_id = " + adminId);
        executeUpdate("DELETE FROM cohort WHERE cohort_id = " + newCohortId);
        executeUpdate("DELETE FROM cohort WHERE cohort_id = " + cohortId);
    }

    @Test
    void 이력_삽입이_실패하면_팀_변경도_롤백된다() {
        // given — 이력 삽입만 실패하도록 만든다
        willThrow(new IllegalStateException("이력 저장 실패"))
                .given(memberHistoryMapper).insertTeamHistory(any());

        // when
        assertThatThrownBy(() -> memberService.changeTeam(adminId,
                new TeamChangeParam(memberId, stageTeamId, "팀 재배치")))
                .isInstanceOf(IllegalStateException.class);

        // then — 이미 실행된 updateTeam이 함께 취소되어 원래 팀이 유지된다
        assertThat(memberMapper.lookupById(memberId))
                .isPresent()
                .get()
                .extracting(Member::getTeamId)
                .isEqualTo(actorTeamId);
    }

    @Test
    void 기수_이력_삽입이_실패하면_기수_변경도_롤백된다() {
        // given
        willThrow(new IllegalStateException("기수 이력 저장 실패"))
                .given(memberHistoryMapper).insertCohortHistory(any());

        // when
        assertThatThrownBy(() -> memberService.changeCohort(adminId,
                new CohortChangeParam(memberId, newCohortId, "기수 정정")))
                .isInstanceOf(IllegalStateException.class);

        // then
        assertThat(memberMapper.lookupById(memberId))
                .isPresent()
                .get()
                .extracting(Member::getCohortId)
                .isEqualTo(cohortId);
    }

    @Test
    void 상태_이력_삽입이_실패하면_상태_변경도_롤백된다() {
        // given
        willThrow(new IllegalStateException("상태 이력 저장 실패"))
                .given(memberHistoryMapper).insertStatusHistory(any());

        // when
        assertThatThrownBy(() -> memberService.changeStatus(adminId,
                new StatusChangeParam(memberId, MemberStatus.REGISTRATION_CANCELLED, "합격 취소")))
                .isInstanceOf(IllegalStateException.class);

        // then
        assertThat(memberMapper.lookupById(memberId))
                .isPresent()
                .get()
                .extracting(Member::getStatus)
                .isEqualTo(MemberStatus.PRE_REGISTERED);
    }

    private void executeUpdate(String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
}
