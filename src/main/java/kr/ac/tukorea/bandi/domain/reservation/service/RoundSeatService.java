package kr.ac.tukorea.bandi.domain.reservation.service;

import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundService;
import kr.ac.tukorea.bandi.domain.reservation.dto.request.RoundSeatCreateParam;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.RoundSeatResponse;
import kr.ac.tukorea.bandi.domain.reservation.exception.DuplicateRoundSeatException;
import kr.ac.tukorea.bandi.domain.reservation.exception.RoundSeatNotFoundException;
import kr.ac.tukorea.bandi.domain.reservation.mapper.ReservationMapper;
import kr.ac.tukorea.bandi.domain.reservation.model.PerformanceRoundSeat;
import kr.ac.tukorea.bandi.domain.reservation.model.RoundSeatStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoundSeatService {

    private final ReservationMapper reservationMapper;
    private final PerformanceRoundService roundService;

    @Transactional
    public Long create(Long actorMemberId, RoundSeatCreateParam param) {
        roundService.validateManage(actorMemberId,
                param.performanceRoundId(), param.performanceProjectId());
        PerformanceRoundSeat seat = PerformanceRoundSeat.available(
                param.performanceRoundId(), param.seatLabel(),
                param.sectionCode(), param.rowLabel(), param.columnLabel(),
                param.displayRow(), param.displayColumn(),
                param.accessibilityCode());
        try {
            reservationMapper.insertRoundSeat(seat);
        } catch (DuplicateKeyException exception) {
            throw new DuplicateRoundSeatException(
                    param.performanceRoundId(), param.seatLabel());
        }
        return seat.getPerformanceRoundSeatId();
    }

    @Transactional
    public void changeStatus(
            Long actorMemberId, Long performanceProjectId,
            Long performanceRoundSeatId, RoundSeatStatus status) {
        PerformanceRoundSeat current = lock(performanceRoundSeatId);
        roundService.validateManage(actorMemberId,
                current.getPerformanceRoundId(), performanceProjectId);
        reservationMapper.updateRoundSeatStatus(
                current.changeStatus(status));
    }

    public List<RoundSeatResponse> search(
            Long actorMemberId, Long performanceProjectId,
            Long performanceRoundId) {
        roundService.validateManage(actorMemberId,
                performanceRoundId, performanceProjectId);
        return reservationMapper.searchRoundSeats(performanceRoundId)
                .stream().map(RoundSeatResponse::from).toList();
    }

    public List<RoundSeatResponse> searchAvailable(
            String slug, Long performanceRoundId) {
        if (!roundService.isPublicRound(slug, performanceRoundId)) {
            throw new RoundSeatNotFoundException(performanceRoundId);
        }
        return reservationMapper.searchAvailableRoundSeats(
                        performanceRoundId)
                .stream().map(RoundSeatResponse::from).toList();
    }

    private PerformanceRoundSeat lock(Long performanceRoundSeatId) {
        return reservationMapper.lookupRoundSeatForUpdate(
                        performanceRoundSeatId)
                .orElseThrow(() -> new RoundSeatNotFoundException(
                        performanceRoundSeatId));
    }
}
