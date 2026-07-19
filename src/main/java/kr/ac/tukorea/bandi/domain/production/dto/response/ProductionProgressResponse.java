package kr.ac.tukorea.bandi.domain.production.dto.response;

public record ProductionProgressResponse(
        Long teamId,
        String teamName,
        int totalCount,
        int completedCount,
        int blockedCount,
        int overdueCount
) {
}
