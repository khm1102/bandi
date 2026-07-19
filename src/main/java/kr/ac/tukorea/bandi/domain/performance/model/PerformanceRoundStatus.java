package kr.ac.tukorea.bandi.domain.performance.model;

public enum PerformanceRoundStatus {
    SCHEDULED,
    RESERVATION_OPEN,
    RESERVATION_CLOSED,
    ENTRY_OPEN,
    ENDED,
    CANCELLED;

    public boolean canTransitionTo(PerformanceRoundStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }
        return switch (this) {
            case SCHEDULED -> targetStatus == RESERVATION_OPEN
                    || targetStatus == CANCELLED;
            case RESERVATION_OPEN -> targetStatus == RESERVATION_CLOSED
                    || targetStatus == CANCELLED;
            case RESERVATION_CLOSED -> targetStatus == ENTRY_OPEN
                    || targetStatus == CANCELLED;
            case ENTRY_OPEN -> targetStatus == ENDED
                    || targetStatus == CANCELLED;
            case ENDED, CANCELLED -> false;
        };
    }
}
