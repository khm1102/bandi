package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceProjectException;
import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceProjectStateException;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class PerformanceProject {

    private static final int MAX_TERM_CODE_LENGTH = 20;
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_PLACE_LENGTH = 200;

    private Long performanceProjectId;
    private final short academicYear;
    private final String termCode;
    private final String title;
    private final LocalDate productionStartDate;
    private final LocalDate productionEndDate;
    private final String place;
    private final PerformanceProjectStatus status;
    private final Long createdByMemberId;
    private final Long updatedByMemberId;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;
    private final LocalDateTime deletedDttm;

    public PerformanceProject(Long performanceProjectId, short academicYear,
                              String termCode, String title,
                              LocalDate productionStartDate,
                              LocalDate productionEndDate, String place,
                              PerformanceProjectStatus status,
                              Long createdByMemberId,
                              Long updatedByMemberId,
                              LocalDateTime createdDttm,
                              LocalDateTime updatedDttm,
                              LocalDateTime deletedDttm) {
        validate(academicYear, termCode, title, productionStartDate,
                productionEndDate, place, status,
                createdByMemberId, updatedByMemberId);
        this.performanceProjectId = performanceProjectId;
        this.academicYear = academicYear;
        this.termCode = termCode.strip();
        this.title = title.strip();
        this.productionStartDate = productionStartDate;
        this.productionEndDate = productionEndDate;
        this.place = place.strip();
        this.status = status;
        this.createdByMemberId = createdByMemberId;
        this.updatedByMemberId = updatedByMemberId;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
        this.deletedDttm = deletedDttm;
    }

    public static PerformanceProject planning(short academicYear,
                                               String termCode,
                                               String title,
                                               LocalDate productionStartDate,
                                               LocalDate productionEndDate,
                                               String place,
                                               Long actorMemberId) {
        return new PerformanceProject(null, academicYear, termCode, title,
                productionStartDate, productionEndDate, place,
                PerformanceProjectStatus.PLANNING, actorMemberId,
                actorMemberId, null, null, null);
    }

    public PerformanceProject edit(short newAcademicYear,
                                   String newTermCode, String newTitle,
                                   LocalDate newProductionStartDate,
                                   LocalDate newProductionEndDate,
                                   String newPlace, Long actorMemberId) {
        if (status != PerformanceProjectStatus.PLANNING) {
            throw new InvalidPerformanceProjectStateException("edit");
        }
        return copy(newAcademicYear, newTermCode, newTitle,
                newProductionStartDate, newProductionEndDate, newPlace,
                status, actorMemberId);
    }

    public PerformanceProject changeStatus(
            PerformanceProjectStatus newStatus, Long actorMemberId) {
        if (!status.canTransitionTo(newStatus)) {
            throw new InvalidPerformanceProjectStateException(
                    "%s-to-%s".formatted(status, newStatus));
        }
        return copy(academicYear, termCode, title, productionStartDate,
                productionEndDate, place, newStatus, actorMemberId);
    }

    private PerformanceProject copy(short newAcademicYear,
                                    String newTermCode, String newTitle,
                                    LocalDate newProductionStartDate,
                                    LocalDate newProductionEndDate,
                                    String newPlace,
                                    PerformanceProjectStatus newStatus,
                                    Long actorMemberId) {
        return new PerformanceProject(performanceProjectId, newAcademicYear,
                newTermCode, newTitle, newProductionStartDate,
                newProductionEndDate, newPlace, newStatus,
                createdByMemberId, actorMemberId, createdDttm,
                updatedDttm, deletedDttm);
    }

    private void validate(short targetAcademicYear, String targetTermCode,
                          String targetTitle,
                          LocalDate targetProductionStartDate,
                          LocalDate targetProductionEndDate,
                          String targetPlace,
                          PerformanceProjectStatus targetStatus,
                          Long creatorId, Long updaterId) {
        if (targetAcademicYear < 1
                || invalidText(targetTermCode, MAX_TERM_CODE_LENGTH)
                || invalidText(targetTitle, MAX_TITLE_LENGTH)
                || invalidText(targetPlace, MAX_PLACE_LENGTH)
                || targetProductionStartDate == null
                || targetProductionEndDate == null
                || targetProductionEndDate.isBefore(targetProductionStartDate)
                || targetStatus == null || creatorId == null
                || updaterId == null) {
            throw new InvalidPerformanceProjectException("required");
        }
    }

    private boolean invalidText(String value, int maxLength) {
        return value == null || value.isBlank()
                || value.strip().length() > maxLength;
    }
}
