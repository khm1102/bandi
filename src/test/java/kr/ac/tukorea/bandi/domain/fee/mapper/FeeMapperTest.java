package kr.ac.tukorea.bandi.domain.fee.mapper;

import kr.ac.tukorea.bandi.domain.fee.dto.response.FeeChargeResponse;
import kr.ac.tukorea.bandi.domain.fee.dto.response.FeeItemResponse;
import kr.ac.tukorea.bandi.domain.fee.dto.response.MemberFeeResponse;
import kr.ac.tukorea.bandi.domain.fee.model.FeeCharge;
import kr.ac.tukorea.bandi.domain.fee.model.FeeChargeHistory;
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
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MapperTest
class FeeMapperTest {

    private static final LocalDate DUE_DATE = LocalDate.of(2026, 9, 30);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 1, 12, 0);

    private final FeeMapper mapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    private Long teamId;
    private Long adminId;
    private Long firstMemberId;
    private Long secondMemberId;

    @Autowired
    FeeMapperTest(FeeMapper mapper, TeamMapper teamMapper,
                  CohortMapper cohortMapper, MemberMapper memberMapper,
                  JdbcTemplate jdbcTemplate) {
        this.mapper = mapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        teamId = teamMapper.searchAll().stream()
                .filter(team -> team.getName().equals("무대팀"))
                .findFirst().orElseThrow().getTeamId();
        Cohort cohort = new Cohort(null, "26-회비", (short) 2026,
                CohortTerm.SECOND, true);
        cohortMapper.insert(cohort);
        adminId = insertMember("2026000401", "회비 운영진",
                cohort.getCohortId(), ClubRole.ADMIN);
        firstMemberId = insertMember("2026000402", "납부 멤버",
                cohort.getCohortId(), ClubRole.MEMBER);
        secondMemberId = insertMember("2026000403", "미납 멤버",
                cohort.getCohortId(), ClubRole.MEMBER);
    }

    @Test
    void 회비_항목을_저장하고_초안_내용과_상태를_갱신한다() {
        FeeItem item = insertDraft("2학기 회비", 30_000L);

        FeeItem locked = mapper.lookupItemByIdForUpdate(item.getFeeItemId())
                .orElseThrow();
        mapper.updateItem(locked.edit("수정 회비", "수정 설명",
                (short) 2026, "SECOND", 35_000L,
                DUE_DATE.plusDays(1), adminId));

        FeeItemResponse response = mapper.searchItems().get(0);
        assertThat(response.name()).isEqualTo("수정 회비");
        assertThat(response.amount()).isEqualTo(35_000L);
        assertThat(response.targetCount()).isZero();
    }

    @Test
    void 대상_스냅샷은_항목과_멤버별로_한_행만_저장한다() {
        FeeItem item = insertDraft("정기 회비", 30_000L);
        mapper.insertCharges(List.of(
                FeeCharge.unpaid(item.getFeeItemId(), firstMemberId, 30_000L),
                FeeCharge.unpaid(item.getFeeItemId(), secondMemberId, 30_000L)));

        assertThat(mapper.searchChargesByItemForUpdate(item.getFeeItemId()))
                .extracting(FeeCharge::getMemberId)
                .containsExactly(firstMemberId, secondMemberId);
        assertThatThrownBy(() -> mapper.insertCharges(List.of(
                FeeCharge.unpaid(item.getFeeItemId(), firstMemberId, 30_000L))))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void 수납_처리와_이력을_저장하고_항목별_집계를_조회한다() {
        FeeItem item = insertOpenItem("정기 회비", 30_000L,
                firstMemberId, secondMemberId);
        List<FeeCharge> charges = mapper.searchChargesByItemForUpdate(
                item.getFeeItemId());
        FeeCharge first = charges.get(0);
        FeeCharge paid = first.changeStatus(
                FeeChargeStatus.PAID, adminId, NOW, "입금 확인");
        mapper.updateCharge(paid);
        mapper.insertChargeHistory(FeeChargeHistory.change(
                first.getFeeChargeId(), first.getStatus(), paid.getStatus(),
                paid.getChargedAmount(), paid.getProcessNote(), adminId, NOW));

        assertThat(mapper.searchItems()).singleElement().satisfies(response -> {
            assertThat(response.targetCount()).isEqualTo(2);
            assertThat(response.paidCount()).isEqualTo(1);
            assertThat(response.unpaidCount()).isEqualTo(1);
            assertThat(response.paidAmount()).isEqualTo(30_000L);
        });
        assertThat(mapper.searchCharges(item.getFeeItemId()))
                .filteredOn(response -> response.status() == FeeChargeStatus.PAID)
                .singleElement()
                .extracting(FeeChargeResponse::processedByName)
                .isEqualTo("회비 운영진");
        assertThat(mapper.searchChargeHistories(first.getFeeChargeId()))
                .singleElement().satisfies(history -> {
                    assertThat(history.changedByName()).isEqualTo("회비 운영진");
                    assertThat(history.newStatus()).isEqualTo(FeeChargeStatus.PAID);
                });
    }

    @Test
    void 멤버는_취소를_제외한_본인_부과와_납부_미납_요약을_조회한다() {
        FeeItem regular = insertOpenItem("정기 회비", 30_000L,
                firstMemberId, secondMemberId);
        FeeCharge regularCharge = mapper.searchChargesByItemForUpdate(
                regular.getFeeItemId()).stream()
                .filter(charge -> charge.getMemberId().equals(firstMemberId))
                .findFirst().orElseThrow();
        mapper.updateCharge(regularCharge.changeStatus(
                FeeChargeStatus.PAID, adminId, NOW, null));

        FeeItem support = insertOpenItem("지원금 면제", 10_000L,
                firstMemberId);
        FeeCharge exemptCharge = mapper.searchChargesByItemForUpdate(
                support.getFeeItemId()).get(0);
        mapper.updateCharge(exemptCharge.changeStatus(
                FeeChargeStatus.EXEMPT, adminId, NOW, "면제"));

        assertThat(mapper.searchMemberFees(firstMemberId))
                .extracting(MemberFeeResponse::status)
                .containsExactly(FeeChargeStatus.EXEMPT, FeeChargeStatus.PAID);
        assertThat(mapper.searchMemberFees(firstMemberId))
                .allSatisfy(response -> {
                    assertThat(response.referenceYear()).isEqualTo((short) 2026);
                    assertThat(response.referenceTermCode()).isEqualTo("SECOND");
                });
        assertThat(mapper.lookupMemberSummary(firstMemberId))
                .satisfies(summary -> {
                    assertThat(summary.totalAmount()).isEqualTo(30_000L);
                    assertThat(summary.paidAmount()).isEqualTo(30_000L);
                    assertThat(summary.unpaidAmount()).isZero();
                });
        assertThat(mapper.lookupMemberSummary(secondMemberId).unpaidAmount())
                .isEqualTo(30_000L);
    }

    @Test
    void 논리_삭제한_항목은_관리와_멤버_조회에서_제외한다() {
        FeeItem item = insertOpenItem("삭제 회비", 30_000L, firstMemberId);
        Long chargeId = mapper.searchChargesByItemForUpdate(
                item.getFeeItemId()).get(0).getFeeChargeId();
        mapper.insertChargeHistory(FeeChargeHistory.change(
                chargeId, FeeChargeStatus.UNPAID, FeeChargeStatus.PAID,
                30_000L, null, adminId, NOW));
        jdbcTemplate.update("""
                UPDATE fee_item SET deleted_dttm = NOW(6)
                WHERE fee_item_id = ?
                """, item.getFeeItemId());

        assertThat(mapper.lookupItemByIdForUpdate(item.getFeeItemId())).isEmpty();
        assertThat(mapper.searchItems()).isEmpty();
        assertThat(mapper.searchCharges(item.getFeeItemId())).isEmpty();
        assertThat(mapper.searchChargeHistories(chargeId)).isEmpty();
        assertThat(mapper.searchMemberFees(firstMemberId)).isEmpty();
        assertThat(mapper.lookupMemberSummary(firstMemberId).totalAmount()).isZero();
    }

    @Test
    void DB는_금액_상태_처리정보와_이력_변경을_제약한다() {
        FeeItem item = insertDraft("제약 회비", 30_000L);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO fee_charge (
                    fee_item_id, member_id, charged_amount, status_code,
                    paid_dttm, processed_by_member_id
                ) VALUES (?, ?, 0, 'UNPAID', NULL, NULL)
                """, item.getFeeItemId(), firstMemberId))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO fee_charge (
                    fee_item_id, member_id, charged_amount, status_code,
                    paid_dttm, processed_by_member_id
                ) VALUES (?, ?, 30000, 'PAID', NULL, ?)
                """, item.getFeeItemId(), firstMemberId, adminId))
                .isInstanceOf(DataAccessException.class);
    }

    private FeeItem insertDraft(String name, long amount) {
        FeeItem item = FeeItem.draft(name, null, (short) 2026,
                "SECOND", amount, DUE_DATE, adminId);
        mapper.insertItem(item);
        return item;
    }

    private FeeItem insertOpenItem(String name, long amount,
                                   Long... memberIds) {
        FeeItem item = insertDraft(name, amount);
        mapper.insertCharges(java.util.Arrays.stream(memberIds)
                .map(memberId -> FeeCharge.unpaid(
                        item.getFeeItemId(), memberId, amount))
                .toList());
        FeeItem opened = item.open(adminId);
        mapper.updateItem(opened);
        return opened;
    }

    private Long insertMember(String studentNo, String name, Long cohortId,
                              ClubRole role) {
        Member member = new Member(null, studentNo, name, null, null, null,
                teamId, cohortId, role, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(member);
        return member.getMemberId();
    }
}
