package kr.ac.tukorea.bandi.domain.performance.service;

import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformancePublicPageStatusParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformancePublicPageWriteParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceViewingGuideWriteParam;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformancePublicPageResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceViewingGuideResponse;
import kr.ac.tukorea.bandi.domain.performance.exception.DuplicatePerformancePublicPageException;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceAccessDeniedException;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformancePublicPageNotFoundException;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformancePublicPageMapper;
import kr.ac.tukorea.bandi.domain.performance.model.PerformancePublicPage;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceViewingGuide;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformancePublicPageService {

    private final PerformancePublicPageMapper publicPageMapper;
    private final MemberService memberService;
    private final PerformanceProjectService performanceProjectService;
    private final FileService fileService;
    private final Clock clock;

    @Transactional
    public Long create(Long actorMemberId,
                       PerformancePublicPageWriteParam param) {
        validateAdmin(actorMemberId);
        performanceProjectService.validateExists(
                actorMemberId, param.performanceProjectId());
        validateImages(param);
        PerformancePublicPage page = createDraft(param);
        try {
            publicPageMapper.insertPage(page);
        } catch (DuplicateKeyException exception) {
            throw new DuplicatePerformancePublicPageException(param.slug());
        }
        return page.getPerformancePublicPageId();
    }

    @Transactional
    public void update(Long actorMemberId,
                       PerformancePublicPageWriteParam param) {
        validateAdmin(actorMemberId);
        PerformancePublicPage page = lock(param.performancePublicPageId());
        page.validateProject(param.performanceProjectId());
        validateImages(param);
        PerformancePublicPage changed = page.edit(param.slug(),
                param.shortDescription(), param.synopsis(),
                param.directorNote(), param.genre(), param.ageRating(),
                param.runtimeMinutes(), param.intermissionMinutes(),
                param.admissionFee(), param.heroFileId(),
                param.posterFileId(), param.accentColor(),
                param.contactName(), param.contactChannel(),
                param.organizerName(), param.ogTitle(),
                param.ogDescription(), param.ogImageFileId(),
                param.publishStartDttm(), param.publishEndDttm());
        try {
            publicPageMapper.updatePage(changed);
        } catch (DuplicateKeyException exception) {
            throw new DuplicatePerformancePublicPageException(param.slug());
        }
    }

    @Transactional
    public void changeStatus(Long actorMemberId,
                             PerformancePublicPageStatusParam param) {
        validateAdmin(actorMemberId);
        PerformancePublicPage page = lock(
                param.performancePublicPageId());
        publicPageMapper.updatePage(page.changeStatus(param.status()));
    }

    @Transactional
    public void saveViewingGuide(Long actorMemberId,
                                 PerformanceViewingGuideWriteParam param) {
        validateAdmin(actorMemberId);
        performanceProjectService.validateExists(
                actorMemberId, param.performanceProjectId());
        Optional<PerformanceViewingGuide> current = publicPageMapper
                .lookupGuideByProjectForUpdate(param.performanceProjectId());
        if (current.isPresent()) {
            publicPageMapper.updateGuide(current.get().edit(
                    param.entryPolicy(), param.lateEntryPolicy(),
                    param.recordingPolicy(), param.cancellationPolicy(),
                    param.accessibilityPolicy(), param.directions(),
                    param.parkingInformation()));
            return;
        }
        publicPageMapper.insertGuide(PerformanceViewingGuide.create(
                param.performanceProjectId(), param.entryPolicy(),
                param.lateEntryPolicy(), param.recordingPolicy(),
                param.cancellationPolicy(), param.accessibilityPolicy(),
                param.directions(), param.parkingInformation()));
    }

    public List<PerformancePublicPageResponse> search(
            Long actorMemberId) {
        validateAdmin(actorMemberId);
        return publicPageMapper.searchPages();
    }

    public Optional<PerformanceViewingGuideResponse> lookupViewingGuide(
            Long actorMemberId, Long performanceProjectId) {
        validateAdmin(actorMemberId);
        performanceProjectService.validateExists(
                actorMemberId, performanceProjectId);
        return publicPageMapper.lookupGuideByProject(performanceProjectId);
    }

    public PerformancePublicPageResponse lookupPublic(String slug) {
        return publicPageMapper.lookupPublicBySlug(slug, now())
                .orElseThrow(() ->
                        new PerformancePublicPageNotFoundException(
                                "slug=" + slug));
    }

    public Optional<PerformanceViewingGuideResponse>
            lookupPublicViewingGuide(Long performanceProjectId) {
        return publicPageMapper.lookupPublicGuide(
                performanceProjectId, now());
    }

    private PerformancePublicPage createDraft(
            PerformancePublicPageWriteParam param) {
        return PerformancePublicPage.draft(param.performanceProjectId(),
                param.slug(), param.shortDescription(), param.synopsis(),
                param.directorNote(), param.genre(), param.ageRating(),
                param.runtimeMinutes(), param.intermissionMinutes(),
                param.admissionFee(), param.heroFileId(),
                param.posterFileId(), param.accentColor(),
                param.contactName(), param.contactChannel(),
                param.organizerName(), param.ogTitle(),
                param.ogDescription(), param.ogImageFileId(),
                param.publishStartDttm(), param.publishEndDttm());
    }

    private PerformancePublicPage lock(Long performancePublicPageId) {
        return publicPageMapper.lookupPageByIdForUpdate(
                        performancePublicPageId)
                .orElseThrow(() ->
                        new PerformancePublicPageNotFoundException(
                                "performancePublicPageId="
                                        + performancePublicPageId));
    }

    private void validateImages(PerformancePublicPageWriteParam param) {
        validateImage(param.heroFileId());
        validateImage(param.posterFileId());
        validateImage(param.ogImageFileId());
    }

    private void validateImage(Long storedFileId) {
        if (storedFileId != null) {
            fileService.validatePublicImageReady(storedFileId);
        }
    }

    private void validateAdmin(Long actorMemberId) {
        MemberAccessContext access = memberService
                .lookupAccessContext(actorMemberId);
        if (!access.canManageGlobal()) {
            throw new PerformanceAccessDeniedException();
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
