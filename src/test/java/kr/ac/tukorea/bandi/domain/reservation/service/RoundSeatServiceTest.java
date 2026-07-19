package kr.ac.tukorea.bandi.domain.reservation.service;

import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundService;
import kr.ac.tukorea.bandi.domain.reservation.dto.request.RoundSeatCreateParam;
import kr.ac.tukorea.bandi.domain.reservation.exception.DuplicateRoundSeatException;
import kr.ac.tukorea.bandi.domain.reservation.exception.RoundSeatNotFoundException;
import kr.ac.tukorea.bandi.domain.reservation.mapper.ReservationMapper;
import kr.ac.tukorea.bandi.domain.reservation.model.PerformanceRoundSeat;
import kr.ac.tukorea.bandi.domain.reservation.model.RoundSeatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoundSeatServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long PROJECT_ID = 10L;
    private static final Long ROUND_ID = 20L;
    private static final Long SEAT_ID = 30L;

    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private PerformanceRoundService roundService;

    private RoundSeatService service;

    @BeforeEach
    void setUp() {
        service = new RoundSeatService(reservationMapper, roundService);
    }

    @Test
    void 운영진이_회차_좌석을_생성한다() {
        willAnswer(invocation -> {
            assignId(invocation.getArgument(0), SEAT_ID);
            return 1;
        }).given(reservationMapper).insertRoundSeat(any());

        Long result = service.create(ACTOR_ID, createParam());

        assertThat(result).isEqualTo(SEAT_ID);
        verify(roundService).validateManage(
                ACTOR_ID, ROUND_ID, PROJECT_ID);
    }

    @Test
    void 같은_회차의_좌석_라벨_중복을_거부한다() {
        willThrow(new DuplicateKeyException("duplicate"))
                .given(reservationMapper).insertRoundSeat(any());

        assertThatThrownBy(() -> service.create(ACTOR_ID, createParam()))
                .isInstanceOf(DuplicateRoundSeatException.class);
    }

    @Test
    void 운영진이_좌석을_차단한다() {
        given(reservationMapper.lookupRoundSeatForUpdate(SEAT_ID))
                .willReturn(Optional.of(seat(RoundSeatStatus.AVAILABLE)));

        service.changeStatus(ACTOR_ID, PROJECT_ID, SEAT_ID,
                RoundSeatStatus.BLOCKED);

        verify(roundService).validateManage(
                ACTOR_ID, ROUND_ID, PROJECT_ID);
        verify(reservationMapper).updateRoundSeatStatus(any());
    }

    @Test
    void 존재하지_않는_좌석은_변경할_수_없다() {
        given(reservationMapper.lookupRoundSeatForUpdate(SEAT_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeStatus(
                ACTOR_ID, PROJECT_ID, SEAT_ID, RoundSeatStatus.BLOCKED))
                .isInstanceOf(RoundSeatNotFoundException.class);

        verify(reservationMapper, never()).updateRoundSeatStatus(any());
    }

    @Test
    void 운영진은_회차의_모든_좌석을_조회한다() {
        service.search(ACTOR_ID, PROJECT_ID, ROUND_ID);

        verify(roundService).validateManage(
                ACTOR_ID, ROUND_ID, PROJECT_ID);
        verify(reservationMapper).searchRoundSeats(ROUND_ID);
    }

    @Test
    void 외부_관람객은_공개_회차의_신청_가능_좌석만_조회한다() {
        given(roundService.isPublicRound("hamlet", ROUND_ID))
                .willReturn(true);

        service.searchAvailable("hamlet", ROUND_ID);

        verify(reservationMapper).searchAvailableRoundSeats(ROUND_ID);
    }

    @Test
    void 공개_페이지에_없는_회차의_좌석은_조회할_수_없다() {
        given(roundService.isPublicRound("hamlet", ROUND_ID))
                .willReturn(false);

        assertThatThrownBy(() -> service.searchAvailable(
                "hamlet", ROUND_ID))
                .isInstanceOf(RoundSeatNotFoundException.class);
    }

    private RoundSeatCreateParam createParam() {
        return new RoundSeatCreateParam(PROJECT_ID, ROUND_ID,
                "A-1", "A", "1", "1", 0, 0, null);
    }

    private PerformanceRoundSeat seat(RoundSeatStatus status) {
        return new PerformanceRoundSeat(SEAT_ID, ROUND_ID,
                "A-1", "A", "1", "1", 0, 0, status,
                null, null, null);
    }

    private void assignId(Object target, Long value) throws Exception {
        Field field = target.getClass().getDeclaredField(
                "performanceRoundSeatId");
        field.setAccessible(true);
        field.set(target, value);
    }
}
