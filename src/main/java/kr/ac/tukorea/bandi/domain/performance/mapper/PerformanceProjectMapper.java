package kr.ac.tukorea.bandi.domain.performance.mapper;

import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceProjectSearchCondition;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceProjectResponse;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProject;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface PerformanceProjectMapper {

    Optional<PerformanceProject> lookupByIdForUpdate(Long performanceProjectId);

    Optional<PerformanceProjectResponse> lookupCurrent(
            @Param("academicYear") short academicYear,
            @Param("termCode") String termCode);

    List<PerformanceProjectResponse> search(
            PerformanceProjectSearchCondition condition);

    int insert(PerformanceProject project);

    int update(PerformanceProject project);
}
