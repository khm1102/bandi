package kr.ac.tukorea.bandi.domain.performance.mapper;

import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformancePublicPageResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceViewingGuideResponse;
import kr.ac.tukorea.bandi.domain.performance.model.PerformancePublicPage;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceViewingGuide;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PerformancePublicPageMapper {

    Optional<PerformancePublicPage> lookupPageByIdForUpdate(
            Long performancePublicPageId);

    Optional<PerformancePublicPageResponse> lookupPublicBySlug(
            @Param("slug") String slug,
            @Param("currentDttm") LocalDateTime currentDttm);

    Optional<PerformanceViewingGuide> lookupGuideByProjectForUpdate(
            Long performanceProjectId);

    Optional<PerformanceViewingGuideResponse> lookupGuideByProject(
            Long performanceProjectId);

    Optional<PerformanceViewingGuideResponse> lookupPublicGuide(
            @Param("performanceProjectId") Long performanceProjectId,
            @Param("currentDttm") LocalDateTime currentDttm);

    List<PerformancePublicPageResponse> searchPages();

    int insertPage(PerformancePublicPage page);

    int updatePage(PerformancePublicPage page);

    int insertGuide(PerformanceViewingGuide guide);

    int updateGuide(PerformanceViewingGuide guide);
}
