package kr.ac.tukorea.bandi.domain.notice.mapper;

import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeManageSearchCondition;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeReadableSearchCondition;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeContentResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeManageContentResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeManageSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeReadStatusResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNotice;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeAttachment;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InternalNoticeMapper {

    Optional<InternalNotice> lookupById(Long internalNoticeId);

    Optional<InternalNotice> lookupByIdForUpdate(Long internalNoticeId);

    List<InternalNoticeManageSummaryResponse> searchManageable(
            InternalNoticeManageSearchCondition condition);

    Optional<InternalNoticeManageContentResponse> lookupManageContent(Long internalNoticeId);

    List<InternalNoticeSummaryResponse> searchReadable(
            InternalNoticeReadableSearchCondition condition);

    Optional<InternalNoticeContentResponse> lookupReadableContent(
            @Param("internalNoticeId") Long internalNoticeId,
            @Param("currentDttm") LocalDateTime currentDttm,
            @Param("memberTeamId") Long memberTeamId,
            @Param("admin") boolean admin);

    boolean existsReadableAttachment(
            @Param("internalNoticeId") Long internalNoticeId,
            @Param("storedFileId") Long storedFileId,
            @Param("currentDttm") LocalDateTime currentDttm,
            @Param("memberTeamId") Long memberTeamId,
            @Param("admin") boolean admin);

    int upsertRead(@Param("internalNoticeId") Long internalNoticeId,
                   @Param("memberId") Long memberId,
                   @Param("readDttm") LocalDateTime readDttm);

    List<InternalNoticeReadStatusResponse> searchReadStatuses(
            @Param("internalNoticeId") Long internalNoticeId,
            @Param("targetScope") InternalNoticeTargetScope targetScope,
            @Param("teamId") Long teamId);

    int insert(InternalNotice internalNotice);

    int update(InternalNotice internalNotice);

    int insertAttachment(InternalNoticeAttachment attachment);

    int removeAttachments(Long internalNoticeId);

    List<Long> searchAttachmentFileIds(Long internalNoticeId);
}
