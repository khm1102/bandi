package kr.ac.tukorea.bandi.domain.performance.mapper;

import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PerformancePublicNoticeMapper {

    boolean exists(@Param("performanceProjectId") Long performanceProjectId,
                   @Param("publicNoticeId") Long publicNoticeId);

    List<Long> searchNoticeIds(
            @Param("performanceProjectId") Long performanceProjectId);

    List<Long> searchPublicNoticeIds(
            @Param("performanceProjectId") Long performanceProjectId,
            @Param("currentDttm") LocalDateTime currentDttm);

    int insert(@Param("performanceProjectId") Long performanceProjectId,
               @Param("publicNoticeId") Long publicNoticeId);

    int remove(@Param("performanceProjectId") Long performanceProjectId,
               @Param("publicNoticeId") Long publicNoticeId);
}
