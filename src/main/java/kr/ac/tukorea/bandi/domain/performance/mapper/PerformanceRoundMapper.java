package kr.ac.tukorea.bandi.domain.performance.mapper;

import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceRoundAccessibilityResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceRoundResponse;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRound;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRoundAccessibility;

import java.util.List;
import java.util.Optional;

public interface PerformanceRoundMapper {

    Optional<PerformanceRound> lookupRoundForUpdate(
            Long performanceRoundId);

    Optional<PerformanceRoundAccessibility> lookupAccessibilityForUpdate(
            Long performanceRoundAccessibilityId);

    List<PerformanceRoundResponse> searchRounds(Long performanceProjectId);

    List<PerformanceRoundAccessibilityResponse> searchAccessibilities(
            Long performanceRoundId);

    List<PerformanceRoundAccessibilityResponse>
            searchAccessibilitiesByProject(Long performanceProjectId);

    int insertRound(PerformanceRound round);

    int updateRound(PerformanceRound round);

    int insertAccessibility(
            PerformanceRoundAccessibility accessibility);

    int updateAccessibility(
            PerformanceRoundAccessibility accessibility);

    int removeAccessibility(Long performanceRoundAccessibilityId);
}
