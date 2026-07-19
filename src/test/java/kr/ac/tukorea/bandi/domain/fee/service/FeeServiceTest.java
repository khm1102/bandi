package kr.ac.tukorea.bandi.domain.fee.service;

import kr.ac.tukorea.bandi.domain.fee.dto.request.FeeChargeProcessParam;
import kr.ac.tukorea.bandi.domain.fee.dto.request.FeeItemUpdateParam;
import kr.ac.tukorea.bandi.domain.fee.dto.request.FeeItemWriteParam;
import kr.ac.tukorea.bandi.domain.fee.dto.request.FeeOpenParam;
import kr.ac.tukorea.bandi.domain.fee.dto.response.MemberFeeSummaryResponse;
import kr.ac.tukorea.bandi.domain.fee.exception.FeeAccessDeniedException;
import kr.ac.tukorea.bandi.domain.fee.exception.FeeChargeNotFoundException;
import kr.ac.tukorea.bandi.domain.fee.exception.FeeItemNotFoundException;
import kr.ac.tukorea.bandi.domain.fee.exception.InvalidFeeException;
import kr.ac.tukorea.bandi.domain.fee.exception.InvalidFeeStateException;
import kr.ac.tukorea.bandi.domain.fee.mapper.FeeMapper;
import kr.ac.tukorea.bandi.domain.fee.model.FeeCharge;
import kr.ac.tukorea.bandi.domain.fee.model.FeeChargeHistory;
import kr.ac.tukorea.bandi.domain.fee.model.FeeChargeStatus;
import kr.ac.tukorea.bandi.domain.fee.model.FeeItem;
import kr.ac.tukorea.bandi.domain.fee.model.FeeItemStatus;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class FeeServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long ITEM_ID = 10L;
    private static final Long TEAM_ID = 4L;
    private static final LocalDate DUE_DATE = LocalDate.of(2026, 9, 30);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 1, 12, 0);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-09-01T03:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Mock
    private FeeMapper feeMapper;
    @Mock
    private MemberService memberService;

    private FeeService service;

    @BeforeEach
    void setUp() {
        service = new FeeService(feeMapper, memberService, CLOCK);
    }

    @Test
    void ADMIN은_회비_초안을_생성하고_ID를_반환한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        willAnswer(invocation -> {
            assignId(invocation.getArgument(0), "feeItemId", ITEM_ID);
            return 1;
        }).given(feeMapper).insertItem(any());

        Long result = service.create(ACTOR_ID, writeParam());

        assertThat(result).isEqualTo(ITEM_ID);
        ArgumentCaptor<FeeItem> captor = ArgumentCaptor.forClass(FeeItem.class);
        verify(feeMapper).insertItem(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(FeeItemStatus.DRAFT);
    }

    @Test
    void ADMIN이_아니면_회비를_관리할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());

        assertThatThrownBy(() -> service.create(ACTOR_ID, writeParam()))
                .isInstanceOf(FeeAccessDeniedException.class);

        verify(feeMapper, never()).insertItem(any());
    }

    @Test
    void DRAFT_항목만_수정할_수_있다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(feeMapper.lookupItemByIdForUpdate(ITEM_ID))
                .willReturn(Optional.of(item(FeeItemStatus.DRAFT)));

        service.update(ACTOR_ID, new FeeItemUpdateParam(
                ITEM_ID, "수정 회비", "수정", (short) 2026,
                "SECOND", 35_000L, DUE_DATE.plusDays(1)));

        ArgumentCaptor<FeeItem> captor = ArgumentCaptor.forClass(FeeItem.class);
        verify(feeMapper).updateItem(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("수정 회비");
    }

    @Test
    void 대상이_비어있으면_전체_활성_멤버를_금액과_함께_스냅샷한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(feeMapper.lookupItemByIdForUpdate(ITEM_ID))
                .willReturn(Optional.of(item(FeeItemStatus.DRAFT)));
        given(memberService.searchActiveMemberIds(null)).willReturn(List.of(2L, 3L));

        int count = service.open(ACTOR_ID, new FeeOpenParam(ITEM_ID, List.of()));

        assertThat(count).isEqualTo(2);
        ArgumentCaptor<List<FeeCharge>> captor = ArgumentCaptor.captor();
        verify(feeMapper).insertCharges(captor.capture());
        assertThat(captor.getValue())
                .extracting(FeeCharge::getMemberId)
                .containsExactly(2L, 3L);
        assertThat(captor.getValue())
                .extracting(FeeCharge::getChargedAmount)
                .containsOnly(30_000L);
        verify(feeMapper).updateItem(any(FeeItem.class));
    }

    @Test
    void 선택_대상은_중복없이_모두_활성인_멤버만_허용한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(feeMapper.lookupItemByIdForUpdate(ITEM_ID))
                .willReturn(Optional.of(item(FeeItemStatus.DRAFT)));
        given(memberService.searchActiveMemberIds(null)).willReturn(List.of(2L, 3L, 4L));

        assertThat(service.open(ACTOR_ID,
                new FeeOpenParam(ITEM_ID, List.of(4L, 2L))))
                .isEqualTo(2);
        assertThatThrownBy(() -> service.open(ACTOR_ID,
                new FeeOpenParam(ITEM_ID, List.of(2L, 2L))))
                .isInstanceOf(InvalidFeeException.class);
        assertThatThrownBy(() -> service.open(ACTOR_ID,
                new FeeOpenParam(ITEM_ID, List.of(9L))))
                .isInstanceOf(InvalidFeeException.class);
    }

    @Test
    void 활성_대상이_없으면_항목을_OPEN하지_않는다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(feeMapper.lookupItemByIdForUpdate(ITEM_ID))
                .willReturn(Optional.of(item(FeeItemStatus.DRAFT)));
        given(memberService.searchActiveMemberIds(null)).willReturn(List.of());

        assertThatThrownBy(() -> service.open(
                ACTOR_ID, new FeeOpenParam(ITEM_ID, null)))
                .isInstanceOf(InvalidFeeException.class);

        verify(feeMapper, never()).insertCharges(any());
        verify(feeMapper, never()).updateItem(any());
    }

    @Test
    void OPEN_항목을_마감한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(feeMapper.lookupItemByIdForUpdate(ITEM_ID))
                .willReturn(Optional.of(item(FeeItemStatus.OPEN)));

        service.close(ACTOR_ID, ITEM_ID);

        ArgumentCaptor<FeeItem> captor = ArgumentCaptor.forClass(FeeItem.class);
        verify(feeMapper).updateItem(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(FeeItemStatus.CLOSED);
    }

    @Test
    void OPEN과_CLOSED에서_개별_또는_일괄_수납을_처리하고_이력을_남긴다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(feeMapper.lookupItemByIdForUpdate(ITEM_ID))
                .willReturn(Optional.of(item(FeeItemStatus.CLOSED)));
        given(feeMapper.searchChargesByIdsForUpdate(
                ITEM_ID, List.of(101L, 102L))).willReturn(List.of(
                charge(101L, 2L, FeeChargeStatus.UNPAID),
                charge(102L, 3L, FeeChargeStatus.UNPAID)));

        int count = service.processCharges(ACTOR_ID,
                new FeeChargeProcessParam(ITEM_ID, List.of(101L, 102L),
                        FeeChargeStatus.PAID, "입금 확인"));

        assertThat(count).isEqualTo(2);
        verify(feeMapper, times(2)).updateCharge(any());
        ArgumentCaptor<FeeChargeHistory> historyCaptor =
                ArgumentCaptor.forClass(FeeChargeHistory.class);
        verify(feeMapper, times(2)).insertChargeHistory(historyCaptor.capture());
        assertThat(historyCaptor.getAllValues())
                .extracting(FeeChargeHistory::getNewStatus)
                .containsOnly(FeeChargeStatus.PAID);
        assertThat(historyCaptor.getAllValues())
                .extracting(FeeChargeHistory::getChangedDttm)
                .containsOnly(NOW);
    }

    @Test
    void 중복_ID나_다른_항목의_charge는_처리하지_않는다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(feeMapper.lookupItemByIdForUpdate(ITEM_ID))
                .willReturn(Optional.of(item(FeeItemStatus.OPEN)));

        assertThatThrownBy(() -> service.processCharges(ACTOR_ID,
                new FeeChargeProcessParam(ITEM_ID, List.of(101L, 101L),
                        FeeChargeStatus.PAID, null)))
                .isInstanceOf(InvalidFeeException.class);

        given(feeMapper.searchChargesByIdsForUpdate(
                ITEM_ID, List.of(101L, 102L)))
                .willReturn(List.of(charge(101L, 2L, FeeChargeStatus.UNPAID)));
        assertThatThrownBy(() -> service.processCharges(ACTOR_ID,
                new FeeChargeProcessParam(ITEM_ID, List.of(101L, 102L),
                        FeeChargeStatus.PAID, null)))
                .isInstanceOf(FeeChargeNotFoundException.class);
    }

    @Test
    void 항목을_취소하면_현재_부과도_취소하고_이력을_남긴다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(feeMapper.lookupItemByIdForUpdate(ITEM_ID))
                .willReturn(Optional.of(item(FeeItemStatus.OPEN)));
        given(feeMapper.searchChargesByItemForUpdate(ITEM_ID)).willReturn(List.of(
                charge(101L, 2L, FeeChargeStatus.UNPAID),
                charge(102L, 3L, FeeChargeStatus.PAID),
                charge(103L, 4L, FeeChargeStatus.CANCELLED)));

        service.cancel(ACTOR_ID, ITEM_ID, "항목 취소");

        verify(feeMapper, times(2)).updateCharge(any());
        verify(feeMapper, times(2)).insertChargeHistory(any());
        ArgumentCaptor<FeeItem> itemCaptor = ArgumentCaptor.forClass(FeeItem.class);
        verify(feeMapper).updateItem(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getStatus())
                .isEqualTo(FeeItemStatus.CANCELLED);
    }

    @Test
    void 운영진은_전체와_이력을_조회하고_활성_멤버는_본인_회비만_조회한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());

        assertThat(service.searchItems(ACTOR_ID)).isEmpty();
        assertThat(service.searchCharges(ACTOR_ID, ITEM_ID)).isEmpty();
        assertThat(service.searchChargeHistories(ACTOR_ID, 101L)).isEmpty();

        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(feeMapper.lookupMemberSummary(ACTOR_ID))
                .willReturn(new MemberFeeSummaryResponse(60_000L, 30_000L, 30_000L));
        assertThat(service.searchMyFees(ACTOR_ID)).isEmpty();
        assertThat(service.lookupMySummary(ACTOR_ID).unpaidAmount())
                .isEqualTo(30_000L);
        assertThatThrownBy(() -> service.searchItems(ACTOR_ID))
                .isInstanceOf(FeeAccessDeniedException.class);
    }

    @Test
    void 존재하지_않는_항목은_변경할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(feeMapper.lookupItemByIdForUpdate(ITEM_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.close(ACTOR_ID, ITEM_ID))
                .isInstanceOf(FeeItemNotFoundException.class);
    }

    private FeeItemWriteParam writeParam() {
        return new FeeItemWriteParam("2학기 회비", "정기 회비",
                (short) 2026, "SECOND", 30_000L, DUE_DATE);
    }

    private FeeItem item(FeeItemStatus status) {
        return new FeeItem(ITEM_ID, "2학기 회비", "정기 회비",
                (short) 2026, "SECOND", 30_000L, DUE_DATE, status,
                ACTOR_ID, ACTOR_ID, null, null, null);
    }

    private FeeCharge charge(Long chargeId, Long memberId,
                             FeeChargeStatus status) {
        LocalDateTime paidDttm = status == FeeChargeStatus.PAID ? NOW : null;
        Long processorId = status == FeeChargeStatus.UNPAID ? null : ACTOR_ID;
        return new FeeCharge(chargeId, ITEM_ID, memberId, 30_000L,
                status, paidDttm, processorId, null, null, null);
    }

    private MemberAccessContext adminContext() {
        return new MemberAccessContext(ACTOR_ID, TEAM_ID,
                true, false, true);
    }

    private MemberAccessContext memberContext() {
        return new MemberAccessContext(ACTOR_ID, TEAM_ID,
                false, false, true);
    }

    private void assignId(Object target, String fieldName, Long value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
