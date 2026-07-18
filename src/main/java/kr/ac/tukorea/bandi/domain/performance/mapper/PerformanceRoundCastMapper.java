package kr.ac.tukorea.bandi.domain.performance.mapper;

import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceRoundCastResponse;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRoundCast;

import java.util.List;
import java.util.Optional;

public interface PerformanceRoundCastMapper {

    Optional<PerformanceRoundCast> lookupByIdForUpdate(
            Long performanceRoundCastId);

    List<PerformanceRoundCastResponse> searchByRound(
            Long performanceRoundId);

    int insert(PerformanceRoundCast cast);

    int update(PerformanceRoundCast cast);

    int remove(Long performanceRoundCastId);
}
