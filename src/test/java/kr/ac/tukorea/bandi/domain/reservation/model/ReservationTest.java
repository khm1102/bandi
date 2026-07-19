package kr.ac.tukorea.bandi.domain.reservation.model;

import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationException;
import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationStateException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    private static final LocalDateTime AGREED =
            LocalDateTime.of(2026, 11, 1, 10, 0);
    private static final byte[] NAME = {1, 2, 3};
    private static final byte[] PHONE = {4, 5, 6};

    @Test
    void 관람_신청을_확정한다() {
        Reservation reservation = reservation();

        assertThat(reservation.getPerformanceRoundId()).isEqualTo(11L);
        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(reservation.getAgreedDttm()).isEqualTo(AGREED);
    }

    @Test
    void 토큰_해시와_암호문은_필수다() {
        assertThatThrownBy(() -> Reservation.confirm(
                11L, "R20261101ABC", null, hash('b'),
                NAME, PHONE, hash('c'), (short) 1, 7L, AGREED))
                .isInstanceOf(InvalidReservationException.class);
    }

    @Test
    void 암호문_배열은_외부에서_변경할_수_없다() {
        Reservation reservation = reservation();
        byte[] exposed = reservation.getApplicantNameCiphertext();

        exposed[0] = 99;

        assertThat(reservation.getApplicantNameCiphertext()[0])
                .isEqualTo((byte) 1);
    }

    @Test
    void 신청을_전체_취소한다() {
        LocalDateTime cancelled = AGREED.plusDays(1);

        Reservation result = reservation().cancel("일정 변경", cancelled);

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(result.getCancelledDttm()).isEqualTo(cancelled);
        assertThat(result.getCancelReason()).isEqualTo("일정 변경");
    }

    @Test
    void 취소_사유는_필수다() {
        assertThatThrownBy(() -> reservation().cancel(" ", AGREED))
                .isInstanceOf(InvalidReservationException.class);
    }

    @Test
    void 이미_취소한_신청은_다시_취소할_수_없다() {
        Reservation cancelled = reservation().cancel("취소", AGREED);

        assertThatThrownBy(() -> cancelled.cancel("다시 취소", AGREED))
                .isInstanceOf(InvalidReservationStateException.class);
    }

    @Test
    void 개인정보를_파기하면_암호문과_토큰_해시를_제거한다() {
        LocalDateTime erased = AGREED.plusYears(1);

        Reservation result = reservation().erasePersonalData(erased);

        assertThat(result.getLookupTokenHash()).isNull();
        assertThat(result.getEntryTokenHash()).isNull();
        assertThat(result.getApplicantNameCiphertext()).isNull();
        assertThat(result.getPhoneCiphertext()).isNull();
        assertThat(result.getPhoneSearchHash()).isNull();
        assertThat(result.getPersonalDataErasedDttm()).isEqualTo(erased);
    }

    @Test
    void 개인정보를_두번_파기할_수_없다() {
        Reservation erased = reservation().erasePersonalData(AGREED);

        assertThatThrownBy(() -> erased.erasePersonalData(AGREED))
                .isInstanceOf(InvalidReservationStateException.class);
    }

    @Test
    void 확정_상태에는_취소_정보가_있을_수_없다() {
        assertThatThrownBy(() -> new Reservation(
                1L, 11L, "R20261101ABC", hash('a'), hash('b'),
                NAME, PHONE, hash('c'), (short) 1,
                ReservationStatus.CONFIRMED, 7L, AGREED,
                AGREED.plusDays(1), "잘못된 취소", null,
                AGREED, AGREED))
                .isInstanceOf(InvalidReservationException.class);
    }

    @Test
    void 개인정보_파기는_모든_민감정보를_함께_제거해야_한다() {
        assertThatThrownBy(() -> new Reservation(
                1L, 11L, "R20261101ABC", hash('a'), null,
                null, null, null, (short) 1,
                ReservationStatus.CONFIRMED, 7L, AGREED,
                null, null, AGREED.plusYears(1), AGREED, AGREED))
                .isInstanceOf(InvalidReservationException.class);
    }

    private Reservation reservation() {
        return Reservation.confirm(
                11L, "R20261101ABC", hash('a'), hash('b'),
                NAME, PHONE, hash('c'), (short) 1, 7L, AGREED);
    }

    private String hash(char value) {
        return String.valueOf(value).repeat(64);
    }
}
