package kr.ac.tukorea.bandi.domain.performance.service;

import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeAdminDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.service.PublicNoticeService;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformancePublicNoticeResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformancePublicPageResponse;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceAccessDeniedException;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformancePublicNoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformancePublicNoticeService {

    private final PerformancePublicNoticeMapper publicNoticeMapper;
    private final MemberService memberService;
    private final PerformanceProjectService projectService;
    private final PerformancePublicPageService publicPageService;
    private final PublicNoticeService noticeService;
    private final Clock clock;

    @Transactional
    public void link(Long actorMemberId, Long performanceProjectId,
                     Long publicNoticeId) {
        validateAdmin(actorMemberId);
        projectService.validateExists(actorMemberId, performanceProjectId);
        noticeService.lookupAdmin(actorMemberId, publicNoticeId);
        if (publicNoticeMapper.exists(performanceProjectId, publicNoticeId)) {
            return;
        }
        publicNoticeMapper.insert(performanceProjectId, publicNoticeId);
    }

    @Transactional
    public void unlink(Long actorMemberId, Long performanceProjectId,
                       Long publicNoticeId) {
        validateAdmin(actorMemberId);
        projectService.validateExists(actorMemberId, performanceProjectId);
        publicNoticeMapper.remove(performanceProjectId, publicNoticeId);
    }

    public List<PerformancePublicNoticeResponse> searchManaged(
            Long actorMemberId, Long performanceProjectId) {
        validateAdmin(actorMemberId);
        projectService.validateExists(actorMemberId, performanceProjectId);
        return publicNoticeMapper.searchNoticeIds(performanceProjectId).stream()
                .map(publicNoticeId -> noticeService.lookupAdmin(
                        actorMemberId, publicNoticeId))
                .map(this::toResponse)
                .toList();
    }

    public List<PerformancePublicNoticeResponse> searchPublic(String slug) {
        PerformancePublicPageResponse page = publicPageService
                .lookupPublic(slug);
        LocalDateTime currentDttm = LocalDateTime.now(clock);
        return publicNoticeMapper.searchPublicNoticeIds(
                        page.performanceProjectId(), currentDttm).stream()
                .map(noticeService::lookupPublic)
                .map(this::toResponse)
                .toList();
    }

    private PerformancePublicNoticeResponse toResponse(
            PublicNoticeAdminDetailResponse notice) {
        return new PerformancePublicNoticeResponse(notice.publicNoticeId(),
                notice.categoryCode(), notice.title(), notice.body(),
                notice.pinned(), notice.publishStartDttm(),
                notice.publishEndDttm(), notice.createdByName(),
                notice.updatedDttm());
    }

    private PerformancePublicNoticeResponse toResponse(
            PublicNoticeDetailResponse notice) {
        return new PerformancePublicNoticeResponse(notice.publicNoticeId(),
                notice.categoryCode(), notice.title(), notice.body(),
                notice.pinned(), notice.publishStartDttm(),
                notice.publishEndDttm(), notice.createdByName(),
                notice.updatedDttm());
    }

    private void validateAdmin(Long actorMemberId) {
        if (!memberService.lookupAccessContext(actorMemberId)
                .canManageGlobal()) {
            throw new PerformanceAccessDeniedException();
        }
    }
}
