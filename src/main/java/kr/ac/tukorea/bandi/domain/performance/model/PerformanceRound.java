package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PerformanceRound {

    private Long performanceRoundId;
    private final Long performanceProjectId;
    private final int roundNo;
    private final LocalDateTime startDttm;
    private final LocalDateTime entryStartDttm;
    private final LocalDateTime reservationOpenDttm;
    private final LocalDateTime reservationCloseDttm;
    private final PerformanceRoundStatus status;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public PerformanceRound(
            Long performanceRoundId, Long performanceProjectId,
            Integer roundNo, LocalDateTime startDttm,
            LocalDateTime entryStartDttm,
            LocalDateTime reservationOpenDttm,
            LocalDateTime reservationCloseDttm,
            PerformanceRoundStatus status,
            LocalDateTime createdDttm, LocalDateTime updatedDttm) {
        this.performanceRoundId = performanceRoundId;
        this.performanceProjectId = requireId(
                performanceProjectId, "performanceProjectId");
        this.roundNo = requireRoundNo(roundNo);
        this.startDttm = requireTime(startDttm, "startDttm");
        this.entryStartDttm = requireTime(
                entryStartDttm, "entryStartDttm");
        this.reservationOpenDttm = requireTime(
                reservationOpenDttm, "reservationOpenDttm");
        this.reservationCloseDttm = requireTime(
                reservationCloseDttm, "reservationCloseDttm");
        this.status = requireStatus(status);
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
        validateTimes();
    }

    public static PerformanceRound scheduled(
            Long performanceProjectId, int roundNo,
            LocalDateTime startDttm, LocalDateTime entryStartDttm,
            LocalDateTime reservationOpenDttm,
            LocalDateTime reservationCloseDttm) {
        return new PerformanceRound(null, performanceProjectId, roundNo,
                startDttm, entryStartDttm, reservationOpenDttm,
                reservationCloseDttm, PerformanceRoundStatus.SCHEDULED,
                null, null);
    }

    public PerformanceRound edit(
            int roundNo, LocalDateTime startDttm,
            LocalDateTime entryStartDttm,
            LocalDateTime reservationOpenDttm,
            LocalDateTime reservationCloseDttm) {
        return copy(roundNo, startDttm, entryStartDttm,
                reservationOpenDttm, reservationCloseDttm, status);
    }

    public PerformanceRound changeStatus(PerformanceRoundStatus status) {
        if (status == null || this.status == status) {
            throw new InvalidPerformanceContentException("status");
        }
        return copy(roundNo, startDttm, entryStartDttm,
                reservationOpenDttm, reservationCloseDttm, status);
    }

    public void validateProject(Long performanceProjectId) {
        if (!this.performanceProjectId.equals(performanceProjectId)) {
            throw new InvalidPerformanceContentException(
                    "performanceProjectId");
        }
    }

    public boolean isReservationOpenAt(LocalDateTime currentDttm) {
        return currentDttm != null
                && status == PerformanceRoundStatus.RESERVATION_OPEN
                && !currentDttm.isBefore(reservationOpenDttm)
                && currentDttm.isBefore(reservationCloseDttm);
    }

    public boolean isViewerCancellationOpen() {
        return status != PerformanceRoundStatus.ENTRY_OPEN
                && status != PerformanceRoundStatus.ENDED
                && status != PerformanceRoundStatus.CANCELLED;
    }

    public boolean isEntryOpen() {
        return status == PerformanceRoundStatus.ENTRY_OPEN;
    }

    private PerformanceRound copy(
            int roundNo, LocalDateTime startDttm,
            LocalDateTime entryStartDttm,
            LocalDateTime reservationOpenDttm,
            LocalDateTime reservationCloseDttm,
            PerformanceRoundStatus status) {
        return new PerformanceRound(performanceRoundId,
                performanceProjectId, roundNo, startDttm, entryStartDttm,
                reservationOpenDttm, reservationCloseDttm, status,
                createdDttm, updatedDttm);
    }

    private void validateTimes() {
        if (!reservationOpenDttm.isBefore(reservationCloseDttm)
                || reservationCloseDttm.isAfter(startDttm)
                || entryStartDttm.isAfter(startDttm)) {
            throw new InvalidPerformanceContentException("roundTimes");
        }
    }

    private static Long requireId(Long value, String field) {
        if (value == null || value < 1) {
            throw new InvalidPerformanceContentException(field);
        }
        return value;
    }

    private static int requireRoundNo(Integer value) {
        if (value == null || value < 1) {
            throw new InvalidPerformanceContentException("roundNo");
        }
        return value;
    }

    private static LocalDateTime requireTime(
            LocalDateTime value, String field) {
        if (value == null) {
            throw new InvalidPerformanceContentException(field);
        }
        return value;
    }

    private static PerformanceRoundStatus requireStatus(
            PerformanceRoundStatus value) {
        if (value == null) {
            throw new InvalidPerformanceContentException("status");
        }
        return value;
    }
}
