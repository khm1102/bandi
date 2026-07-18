package kr.ac.tukorea.bandi.domain.performance.dto.request;

public record ProductionCreditWriteParam(
        Long productionCreditId,
        Long performanceProjectId,
        String creditRole,
        String publicName,
        Long publicProfileId,
        int displayOrder
) {
}
