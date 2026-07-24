package kr.ac.tukorea.bandi.domain.resource.mapper;

import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceFileLinkResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceLinkPreviewResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceSummaryResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourcePublicShareResponse;
import kr.ac.tukorea.bandi.domain.resource.model.Resource;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceFile;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface ResourceMapper {

    Optional<Resource> lookupByIdForUpdate(Long resourceId);

    Optional<Resource> lookupById(Long resourceId);

    List<ResourceSummaryResponse> search(@Param("keyword") String keyword,
                                         @Param("limit") int limit,
                                         @Param("offset") long offset);

    long count(@Param("keyword") String keyword);

    Optional<ResourceDetailRow> lookupDetail(Long resourceId);

    List<ResourceFileLinkResponse> searchFiles(Long resourceId);

    List<ResourceLinkPreviewResponse> searchLinkPreviews(Long resourceId);

    List<Long> searchPreviewImageFileIds(Long resourceId);

    boolean existsFile(@Param("resourceId") Long resourceId,
                       @Param("storedFileId") Long storedFileId);

    boolean existsPreviewImage(@Param("resourceId") Long resourceId,
                               @Param("storedFileId") Long storedFileId);

    int insert(Resource resource);

    int update(Resource resource);

    int delete(@Param("resourceId") Long resourceId);

    int removeFiles(Long resourceId);

    int insertFile(ResourceFile resourceFile);

    int removeLinkPreviews(Long resourceId);

    int insertLinkPreview(ResourceLinkPreviewRow preview);

    Optional<String> lookupShareTokenForUpdate(Long resourceId);

    Optional<ResourcePublicShareResponse> lookupPublicShare(String shareToken);

    boolean existsShareToken(Long resourceId);

    int updateShareToken(@Param("resourceId") Long resourceId,
                         @Param("shareToken") String shareToken);

    record ResourceDetailRow(Long resourceId, String title, String bodyMarkdown,
                             String createdByName, String updatedByName,
                             java.time.LocalDateTime createdDttm,
                             java.time.LocalDateTime updatedDttm,
                             Long createdByMemberId) {
    }

    record ResourceLinkPreviewRow(Long resourceId, String normalizedUrl, String urlHash,
                                  String domain, String title, String description,
                                  Long previewImageFileId, java.time.LocalDateTime fetchedDttm) {
    }
}
