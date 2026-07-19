package kr.ac.tukorea.bandi.domain.fee.service;

import kr.ac.tukorea.bandi.domain.fee.dto.request.FeeChargeProcessParam;
import kr.ac.tukorea.bandi.domain.fee.dto.request.FeeOpenParam;
import kr.ac.tukorea.bandi.domain.fee.mapper.FeeMapper;
import kr.ac.tukorea.bandi.domain.fee.model.FeeCharge;
import kr.ac.tukorea.bandi.domain.fee.model.FeeChargeStatus;
import kr.ac.tukorea.bandi.domain.fee.model.FeeItem;
import kr.ac.tukorea.bandi.domain.fee.model.FeeItemStatus;
import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@SpringBootTest
@ActiveProfiles("test")
class FeeServiceTransactionTest {

    private static final LocalDate DUE_DATE = LocalDate.of(2026, 9, 30);

    private final FeeService service;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private FeeMapper mapper;
    @MockitoBean
    private MemberService memberService;
    @MockitoBean
    private Clock clock;

    private Long teamId;
    private Long cohortId;
    private Long adminId;
    private Long memberId;
    private Long draftItemId;
    private Long openItemId;
    private Long chargeId;

    @Autowired
    FeeServiceTransactionTest(FeeService service, TeamMapper teamMapper,
                              CohortMapper cohortMapper,
                              MemberMapper memberMapper,
                              JdbcTemplate jdbcTemplate) {
        this.service = service;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        given(clock.instant()).willReturn(Instant.parse("2026-09-01T03:00:00Z"));
        given(clock.getZone()).willReturn(ZoneId.of("Asia/Seoul"));
        teamId = teamMapper.searchAll().stream()
                .filter(team -> team.getName().equals("무대팀"))
                .findFirst().orElseThrow().getTeamId();
        Cohort cohort = new Cohort(null, "회비트랜잭션기수", (short) 2995,
                CohortTerm.SECOND, true);
        cohortMapper.insert(cohort);
        cohortId = cohort.getCohortId();
        adminId = insertMember("2995000001", "회비운영진", ClubRole.ADMIN);
        memberId = insertMember("2995000002", "회비대상", ClubRole.MEMBER);
        given(memberService.lookupAccessContext(adminId))
                .willReturn(new MemberAccessContext(
                        adminId, teamId, true, false, true));
        given(memberService.searchActiveMemberIds(null))
                .willReturn(List.of(memberId));

        FeeItem draft = insertDraft("열기 롤백");
        draftItemId = draft.getFeeItemId();
        FeeItem open = insertDraft("수납 롤백");
        mapper.insertCharges(List.of(FeeCharge.unpaid(
                open.getFeeItemId(), memberId, open.getAmount())));
        mapper.updateItem(open.open(adminId));
        openItemId = open.getFeeItemId();
        chargeId = jdbcTemplate.queryForObject("""
                SELECT fee_charge_id FROM fee_charge
                WHERE fee_item_id = ? AND member_id = ?
                """, Long.class, openItemId, memberId);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM fee_charge_history");
        jdbcTemplate.update("DELETE FROM fee_charge");
        jdbcTemplate.update("DELETE FROM fee_item");
        jdbcTemplate.update("DELETE FROM member WHERE member_id IN (?, ?)",
                adminId, memberId);
        jdbcTemplate.update("DELETE FROM cohort WHERE cohort_id = ?", cohortId);
    }

    @Test
    void 항목_OPEN_갱신이_실패하면_대상_스냅샷도_롤백한다() {
        willThrow(new IllegalStateException("항목 상태 갱신 실패"))
                .given(mapper).updateItem(any());

        assertThatThrownBy(() -> service.open(adminId,
                new FeeOpenParam(draftItemId, List.of())))
                .isInstanceOf(IllegalStateException.class);

        FeeItem item = mapper.lookupItemByIdForUpdate(draftItemId)
                .orElseThrow();
        assertThat(item.getStatus()).isEqualTo(FeeItemStatus.DRAFT);
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM fee_charge WHERE fee_item_id = ?
                """, Integer.class, draftItemId);
        assertThat(count).isZero();
    }

    @Test
    void 이력_저장이_실패하면_수납_상태도_UNPAID로_롤백한다() {
        willThrow(new IllegalStateException("이력 저장 실패"))
                .given(mapper).insertChargeHistory(any());

        assertThatThrownBy(() -> service.processCharges(adminId,
                new FeeChargeProcessParam(openItemId, List.of(chargeId),
                        FeeChargeStatus.PAID, null)))
                .isInstanceOf(IllegalStateException.class);

        FeeCharge charge = mapper.searchChargesByIdsForUpdate(
                openItemId, List.of(chargeId)).get(0);
        assertThat(charge.getStatus()).isEqualTo(FeeChargeStatus.UNPAID);
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM fee_charge_history
                WHERE fee_charge_id = ?
                """, Integer.class, chargeId);
        assertThat(count).isZero();
    }

    private FeeItem insertDraft(String name) {
        FeeItem item = FeeItem.draft(name, null, (short) 2026,
                "SECOND", 30_000L, DUE_DATE, adminId);
        mapper.insertItem(item);
        return item;
    }

    private Long insertMember(String studentNo, String name, ClubRole role) {
        Member member = new Member(null, studentNo, name, null, null, null,
                teamId, cohortId, role, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(member);
        return member.getMemberId();
    }
}
