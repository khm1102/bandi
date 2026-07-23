package kr.ac.tukorea.bandi.domain.resource.mapper;

import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceManageSearchCondition;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceReadableSearchCondition;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceContentResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceFileLinkResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceManageContentResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceManageSummaryResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceSummaryResponse;
import kr.ac.tukorea.bandi.domain.resource.model.Resource;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceFile;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface ResourceMapper {

    Optional<Resource> lookupById(Long resourceId);

    Optional<Resource> lookupByIdForUpdate(Long resourceId);

    Optional<Integer> lookupMaxRevisionForUpdate(Long resourceId);

    List<ResourceManageSummaryResponse> searchManageable(
            ResourceManageSearchCondition condition);

    long countManageable(ResourceManageSearchCondition condition);

    Optional<ResourceManageContentResponse> lookupManageContent(Long resourceId);

    List<ResourceSummaryResponse> searchReadable(ResourceReadableSearchCondition condition);

    long countReadable(ResourceReadableSearchCondition condition);

    Optional<ResourceContentResponse> lookupReadableContent(
            @Param("resourceId") Long resourceId,
            @Param("memberTeamId") Long memberTeamId,
            @Param("admin") boolean admin);

    boolean existsFilesInRevision(@Param("resourceId") Long resourceId,
                                  @Param("revisionNo") int revisionNo);

    boolean existsReadableCurrentFile(@Param("resourceId") Long resourceId,
                                      @Param("storedFileId") Long storedFileId,
                                      @Param("memberTeamId") Long memberTeamId,
                                      @Param("admin") boolean admin);

    List<ResourceFileLinkResponse> searchCurrentFileLinks(Long resourceId);

    List<ResourceFileLinkResponse> searchFileLinks(Long resourceId);

    int insert(Resource resource);

    int update(Resource resource);

    int insertFile(ResourceFile resourceFile);
}
