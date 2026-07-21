package kr.ac.tukorea.bandi.domain.performance.retirement;

public record PerformanceFileRetirementResult(
        int candidateCount,
        int pendingCount,
        int skippedCount,
        int deletedCount
) {

    public static PerformanceFileRetirementResult report(int candidateCount,
                                                          int pendingCount,
                                                          int skippedCount) {
        return new PerformanceFileRetirementResult(candidateCount, pendingCount,
                skippedCount, 0);
    }

    public PerformanceFileRetirementResult withDeleted(int deletedCount,
                                                        int skippedCount) {
        return new PerformanceFileRetirementResult(candidateCount, pendingCount,
                skippedCount, deletedCount);
    }
}
