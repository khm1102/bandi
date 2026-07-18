package kr.ac.tukorea.bandi.domain.performance.model;

public enum PerformanceProjectStatus {
    PLANNING,
    PRODUCING,
    RESERVATION_OPEN,
    PERFORMING,
    ENDED,
    CANCELLED,
    ARCHIVED;

    public boolean canTransitionTo(PerformanceProjectStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }
        return switch (this) {
            case PLANNING -> targetStatus == PRODUCING
                    || targetStatus == CANCELLED;
            case PRODUCING -> targetStatus == RESERVATION_OPEN
                    || targetStatus == CANCELLED;
            case RESERVATION_OPEN -> targetStatus == PERFORMING
                    || targetStatus == CANCELLED;
            case PERFORMING -> targetStatus == ENDED
                    || targetStatus == CANCELLED;
            case ENDED -> targetStatus == ARCHIVED;
            case CANCELLED, ARCHIVED -> false;
        };
    }
}
