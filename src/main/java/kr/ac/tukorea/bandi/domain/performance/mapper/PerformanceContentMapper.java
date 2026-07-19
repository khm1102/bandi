package kr.ac.tukorea.bandi.domain.performance.mapper;

import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceCastHistoryResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceCastResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceCharacterResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceMediaResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.ProductionCreditResponse;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceCast;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceCastHistory;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceCharacter;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceMedia;
import kr.ac.tukorea.bandi.domain.performance.model.ProductionCredit;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface PerformanceContentMapper {

    Optional<PerformanceCharacter> lookupCharacterForUpdate(
            Long performanceCharacterId);

    Optional<PerformanceCast> lookupCastForUpdate(Long performanceCastId);

    Optional<ProductionCredit> lookupCreditForUpdate(
            Long productionCreditId);

    Optional<PerformanceMedia> lookupMediaForUpdate(Long performanceMediaId);

    boolean existsCastByCharacter(Long performanceCharacterId);

    boolean existsCastHistoryByCharacter(Long performanceCharacterId);

    List<PerformanceCharacterResponse> searchCharacters(
            Long performanceProjectId);

    List<PerformanceCastResponse> searchCasts(Long performanceProjectId);

    List<PerformanceCastHistoryResponse> searchCastHistories(
            Long performanceProjectId);

    List<ProductionCreditResponse> searchCredits(Long performanceProjectId);

    List<PerformanceMediaResponse> searchMedia(
            @Param("performanceProjectId") Long performanceProjectId,
            @Param("publishedOnly") boolean publishedOnly);

    int insertCharacter(PerformanceCharacter character);

    int updateCharacter(PerformanceCharacter character);

    int removeCharacter(Long performanceCharacterId);

    int insertCast(PerformanceCast cast);

    int updateCast(PerformanceCast cast);

    int removeCast(Long performanceCastId);

    int insertCastHistory(PerformanceCastHistory history);

    int insertCredit(ProductionCredit credit);

    int updateCredit(ProductionCredit credit);

    int removeCredit(Long productionCreditId);

    int insertMedia(PerformanceMedia media);

    int updateMedia(PerformanceMedia media);

    int removeMedia(Long performanceMediaId);
}
