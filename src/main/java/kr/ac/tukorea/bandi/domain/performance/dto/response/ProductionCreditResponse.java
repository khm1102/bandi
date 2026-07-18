package kr.ac.tukorea.bandi.domain.performance.dto.response;

public record ProductionCreditResponse(
        Long productionCreditId,
        Long performanceProjectId,
        String creditRole,
        String publicName,
        Long publicProfileId,
        int displayOrder
) {
}
