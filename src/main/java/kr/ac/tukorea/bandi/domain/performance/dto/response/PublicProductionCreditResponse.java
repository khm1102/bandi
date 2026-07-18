package kr.ac.tukorea.bandi.domain.performance.dto.response;

public record PublicProductionCreditResponse(
        Long productionCreditId,
        String creditRole,
        String publicName,
        int displayOrder,
        PublicProfileViewResponse profile
) {

    public static PublicProductionCreditResponse from(
            ProductionCreditResponse credit,
            PublicProfileViewResponse profile) {
        return new PublicProductionCreditResponse(
                credit.productionCreditId(), credit.creditRole(),
                credit.publicName(), credit.displayOrder(), profile);
    }
}
