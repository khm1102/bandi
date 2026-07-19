package kr.ac.tukorea.bandi.domain.notice.mapper;

import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeAdminSearchCondition;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeSearchCondition;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeAdminContentResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeAdminSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeContentResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.model.PublicNotice;
import kr.ac.tukorea.bandi.domain.notice.model.PublicNoticeAttachment;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PublicNoticeMapper {

    Optional<PublicNotice> lookupById(Long publicNoticeId);

    Optional<PublicNotice> lookupByIdForUpdate(Long publicNoticeId);

    List<PublicNoticeSummaryResponse> searchPublic(PublicNoticeSearchCondition condition);

    List<PublicNoticeAdminSummaryResponse> searchAdmin(
            PublicNoticeAdminSearchCondition condition);

    Optional<PublicNoticeContentResponse> lookupPublicContent(
            @Param("publicNoticeId") Long publicNoticeId,
            @Param("currentDttm") LocalDateTime currentDttm);

    Optional<PublicNoticeAdminContentResponse> lookupAdminContent(Long publicNoticeId);

    int insert(PublicNotice publicNotice);

    int update(PublicNotice publicNotice);

    int insertAttachment(PublicNoticeAttachment attachment);

    int removeAttachments(Long publicNoticeId);

    List<Long> searchAttachmentFileIds(Long publicNoticeId);

    boolean existsPublicAttachment(
            @Param("publicNoticeId") Long publicNoticeId,
            @Param("storedFileId") Long storedFileId,
            @Param("currentDttm") LocalDateTime currentDttm);
}
