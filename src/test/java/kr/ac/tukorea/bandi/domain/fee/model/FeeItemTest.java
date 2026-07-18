package kr.ac.tukorea.bandi.domain.fee.model;

import kr.ac.tukorea.bandi.domain.fee.exception.InvalidFeeException;
import kr.ac.tukorea.bandi.domain.fee.exception.InvalidFeeStateException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeeItemTest {

    private static final Long ACTOR_ID = 1L;
    private static final LocalDate DUE_DATE = LocalDate.of(2026, 9, 30);

    @Test
    void 회비_항목_초안을_생성한다() {
        FeeItem item = draft();

        assertThat(item.getStatus()).isEqualTo(FeeItemStatus.DRAFT);
        assertThat(item.getAmount()).isEqualTo(30_000L);
    }

    @Test
    void 항목명_연도_금액_기한을_검증한다() {
        assertThatThrownBy(() -> FeeItem.draft(" ", null, (short) 2026,
                "SECOND", 30_000L, DUE_DATE, ACTOR_ID))
                .isInstanceOf(InvalidFeeException.class);
        assertThatThrownBy(() -> FeeItem.draft("2학기 회비", null,
                (short) 0, "SECOND", 30_000L, DUE_DATE, ACTOR_ID))
                .isInstanceOf(InvalidFeeException.class);
        assertThatThrownBy(() -> FeeItem.draft("2학기 회비", null,
                (short) 2026, "SECOND", 0, DUE_DATE, ACTOR_ID))
                .isInstanceOf(InvalidFeeException.class);
    }

    @Test
    void 초안만_수정할_수_있다() {
        FeeItem edited = draft().edit("수정 회비", "수정",
                (short) 2026, "SECOND", 35_000L,
                DUE_DATE.plusDays(1), ACTOR_ID);

        assertThat(edited.getName()).isEqualTo("수정 회비");
        assertThatThrownBy(() -> draft().open(ACTOR_ID).edit(
                "변경", null, (short) 2026, null,
                1L, DUE_DATE, ACTOR_ID))
                .isInstanceOf(InvalidFeeStateException.class);
    }

    @Test
    void 초안을_OPEN하고_OPEN을_CLOSED한다() {
        FeeItem opened = draft().open(ACTOR_ID);
        FeeItem closed = opened.close(ACTOR_ID);

        assertThat(opened.getStatus()).isEqualTo(FeeItemStatus.OPEN);
        assertThat(closed.getStatus()).isEqualTo(FeeItemStatus.CLOSED);
    }

    @Test
    void 이미_OPEN한_항목은_다시_OPEN할_수_없다() {
        FeeItem opened = draft().open(ACTOR_ID);

        assertThatThrownBy(() -> opened.open(ACTOR_ID))
                .isInstanceOf(InvalidFeeStateException.class);
    }

    @Test
    void 취소한_항목은_다시_변경할_수_없다() {
        FeeItem cancelled = draft().cancel(ACTOR_ID);

        assertThat(cancelled.getStatus()).isEqualTo(FeeItemStatus.CANCELLED);
        assertThatThrownBy(() -> cancelled.open(ACTOR_ID))
                .isInstanceOf(InvalidFeeStateException.class);
    }

    @Test
    void OPEN과_CLOSED에서만_수납_상태를_처리할_수_있다() {
        FeeItem opened = draft().open(ACTOR_ID);
        FeeItem closed = opened.close(ACTOR_ID);

        opened.validateChargeProcessing();
        closed.validateChargeProcessing();
        assertThatThrownBy(() -> draft().validateChargeProcessing())
                .isInstanceOf(InvalidFeeStateException.class);
        assertThatThrownBy(() -> draft().cancel(ACTOR_ID)
                .validateChargeProcessing())
                .isInstanceOf(InvalidFeeStateException.class);
    }

    private FeeItem draft() {
        return FeeItem.draft("2학기 회비", "정기 회비", (short) 2026,
                "SECOND", 30_000L, DUE_DATE, ACTOR_ID);
    }
}
