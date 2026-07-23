package kr.ac.tukorea.bandi.domain.resource.mapper;

import kr.ac.tukorea.bandi.domain.resource.model.ResourceLinkPreviewRetirementManifest;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ResourceLinkPreviewRetirementMapper {

    int insert(ResourceLinkPreviewRetirementManifest manifest);

    List<ResourceLinkPreviewRetirementManifest> searchUncompleted();

    int updateDeleted(@Param("manifestId") Long manifestId,
                      @Param("completedDttm") LocalDateTime completedDttm);

    int updateFailed(@Param("manifestId") Long manifestId,
                     @Param("failureReason") String failureReason);
}
