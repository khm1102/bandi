package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import lombok.Getter;

@Getter
public class ProductionCredit {

    private Long productionCreditId;
    private final Long performanceProjectId;
    private final String creditRole;
    private final String publicName;
    private final Long publicProfileId;
    private final int displayOrder;

    public ProductionCredit(Long productionCreditId,
                            Long performanceProjectId, String creditRole,
                            String publicName, Long publicProfileId,
                            Integer displayOrder) {
        this.productionCreditId = productionCreditId;
        this.performanceProjectId = requireId(performanceProjectId);
        this.creditRole = requireText(creditRole, 100, "creditRole");
        this.publicName = requireText(publicName, 100, "publicName");
        this.publicProfileId = publicProfileId;
        this.displayOrder = requireOrder(displayOrder);
    }

    public static ProductionCredit create(
            Long performanceProjectId, String creditRole,
            String publicName, Long publicProfileId, int displayOrder) {
        return new ProductionCredit(null, performanceProjectId,
                creditRole, publicName, publicProfileId, displayOrder);
    }

    public ProductionCredit edit(String creditRole, String publicName,
                                 Long publicProfileId, int displayOrder) {
        return new ProductionCredit(productionCreditId,
                performanceProjectId, creditRole, publicName,
                publicProfileId, displayOrder);
    }

    private static Long requireId(Long value) {
        if (value == null || value < 1) {
            throw new InvalidPerformanceContentException(
                    "performanceProjectId");
        }
        return value;
    }

    private static String requireText(String value, int maxLength,
                                      String field) {
        if (value == null || value.isBlank()
                || value.strip().length() > maxLength) {
            throw new InvalidPerformanceContentException(field);
        }
        return value.strip();
    }

    private static int requireOrder(Integer value) {
        if (value == null || value < 0) {
            throw new InvalidPerformanceContentException("displayOrder");
        }
        return value;
    }
}
