package kr.ac.tukorea.bandi.domain.production.mapper;

import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskSearchCondition;
import kr.ac.tukorea.bandi.domain.production.dto.response.ProductionProgressResponse;
import kr.ac.tukorea.bandi.domain.production.dto.response.ProductionTaskHistoryResponse;
import kr.ac.tukorea.bandi.domain.production.dto.response.ProductionTaskResponse;
import kr.ac.tukorea.bandi.domain.production.model.ProductionTask;
import kr.ac.tukorea.bandi.domain.production.model.ProductionTaskHistory;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductionTaskMapper {

    Optional<ProductionTask> lookupByIdForUpdate(Long productionTaskId);

    List<ProductionTaskResponse> search(
            @Param("condition") ProductionTaskSearchCondition condition,
            @Param("currentDate") LocalDate currentDate);

    ProductionProgressResponse lookupProjectProgress(
            @Param("performanceProjectId") Long performanceProjectId,
            @Param("currentDate") LocalDate currentDate);

    List<ProductionProgressResponse> searchTeamProgress(
            @Param("performanceProjectId") Long performanceProjectId,
            @Param("currentDate") LocalDate currentDate);

    List<ProductionTaskHistoryResponse> searchHistories(Long productionTaskId);

    int insert(ProductionTask task);

    int update(ProductionTask task);

    int delete(@Param("productionTaskId") Long productionTaskId,
               @Param("actorMemberId") Long actorMemberId,
               @Param("deletedDttm") LocalDateTime deletedDttm);

    int insertHistory(ProductionTaskHistory history);
}
