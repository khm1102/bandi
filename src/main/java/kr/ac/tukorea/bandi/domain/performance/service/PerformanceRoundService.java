package kr.ac.tukorea.bandi.domain.performance.service;

import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundAccessibilityWriteParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundStatusParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundWriteParam;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceRoundAccessibilityResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceRoundResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicPerformanceRoundResponse;
import kr.ac.tukorea.bandi.domain.performance.exception.DuplicatePerformanceContentException;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceAccessDeniedException;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceContentNotFoundException;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformanceRoundMapper;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRound;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRoundAccessibility;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceRoundService {

    private final PerformanceRoundMapper roundMapper;
    private final MemberService memberService;
    private final PerformanceProjectService projectService;
    private final PerformancePublicPageService publicPageService;

    @Transactional
    public Long createRound(Long actorMemberId,
                            PerformanceRoundWriteParam param) {
        validateAdmin(actorMemberId);
        projectService.validateExists(
                actorMemberId, param.performanceProjectId());
        PerformanceRound round = PerformanceRound.scheduled(
                param.performanceProjectId(), param.roundNo(),
                param.startDttm(), param.entryStartDttm(),
                param.reservationOpenDttm(),
                param.reservationCloseDttm());
        try {
            roundMapper.insertRound(round);
        } catch (DuplicateKeyException exception) {
            throw new DuplicatePerformanceContentException("roundNo");
        }
        return round.getPerformanceRoundId();
    }

    @Transactional
    public void updateRound(Long actorMemberId,
                            PerformanceRoundWriteParam param) {
        validateAdmin(actorMemberId);
        PerformanceRound current = lockRound(param.performanceRoundId());
        current.validateProject(param.performanceProjectId());
        PerformanceRound changed = current.edit(param.roundNo(),
                param.startDttm(), param.entryStartDttm(),
                param.reservationOpenDttm(),
                param.reservationCloseDttm());
        try {
            roundMapper.updateRound(changed);
        } catch (DuplicateKeyException exception) {
            throw new DuplicatePerformanceContentException("roundNo");
        }
    }

    @Transactional
    public void changeRoundStatus(Long actorMemberId,
                                  PerformanceRoundStatusParam param) {
        validateAdmin(actorMemberId);
        PerformanceRound current = lockRound(param.performanceRoundId());
        roundMapper.updateRound(current.changeStatus(param.status()));
    }

    public List<PerformanceRoundResponse> searchRounds(
            Long actorMemberId, Long performanceProjectId) {
        validateAdmin(actorMemberId);
        return roundMapper.searchRounds(performanceProjectId);
    }

    @Transactional
    public Long createAccessibility(
            Long actorMemberId,
            PerformanceRoundAccessibilityWriteParam param) {
        validateAdmin(actorMemberId);
        lockRound(param.performanceRoundId());
        PerformanceRoundAccessibility accessibility =
                PerformanceRoundAccessibility.create(
                        param.performanceRoundId(), param.supportType(),
                        param.title(), param.description(),
                        param.displayOrder());
        try {
            roundMapper.insertAccessibility(accessibility);
        } catch (DuplicateKeyException exception) {
            throw new DuplicatePerformanceContentException(
                    "accessibilitySupportType");
        }
        return accessibility.getPerformanceRoundAccessibilityId();
    }

    @Transactional
    public void updateAccessibility(
            Long actorMemberId,
            PerformanceRoundAccessibilityWriteParam param) {
        validateAdmin(actorMemberId);
        PerformanceRoundAccessibility current = lockAccessibility(
                param.performanceRoundAccessibilityId());
        current.validateRound(param.performanceRoundId());
        PerformanceRoundAccessibility changed = current.edit(
                param.supportType(), param.title(), param.description(),
                param.displayOrder());
        try {
            roundMapper.updateAccessibility(changed);
        } catch (DuplicateKeyException exception) {
            throw new DuplicatePerformanceContentException(
                    "accessibilitySupportType");
        }
    }

    @Transactional
    public void removeAccessibility(
            Long actorMemberId, Long performanceRoundAccessibilityId) {
        validateAdmin(actorMemberId);
        lockAccessibility(performanceRoundAccessibilityId);
        roundMapper.removeAccessibility(performanceRoundAccessibilityId);
    }

    public List<PerformanceRoundAccessibilityResponse>
            searchAccessibilities(Long actorMemberId,
                                  Long performanceRoundId) {
        validateAdmin(actorMemberId);
        return roundMapper.searchAccessibilities(performanceRoundId);
    }

    @Transactional
    public void validateExists(Long actorMemberId, Long performanceRoundId,
                               Long performanceProjectId) {
        validateInternal(actorMemberId);
        lockRound(performanceRoundId).validateProject(performanceProjectId);
    }

    @Transactional
    public void validateManage(Long actorMemberId, Long performanceRoundId,
                               Long performanceProjectId) {
        validateAdmin(actorMemberId);
        lockRound(performanceRoundId).validateProject(performanceProjectId);
    }

    public List<PublicPerformanceRoundResponse> searchPublicRounds(
            String slug) {
        Long projectId = publicPageService.lookupPublic(slug)
                .performanceProjectId();
        Map<Long, List<PerformanceRoundAccessibilityResponse>> byRound =
                groupAccessibilities(projectId);
        List<PublicPerformanceRoundResponse> result = new ArrayList<>();
        for (PerformanceRoundResponse round
                : roundMapper.searchRounds(projectId)) {
            result.add(PublicPerformanceRoundResponse.from(round,
                    byRound.getOrDefault(
                            round.performanceRoundId(), List.of())));
        }
        return result;
    }

    public boolean isPublicRound(String slug, Long performanceRoundId) {
        return searchPublicRounds(slug).stream()
                .anyMatch(round -> round.performanceRoundId()
                        .equals(performanceRoundId));
    }

    public boolean isPublicReservationOpen(
            String slug, Long performanceRoundId,
            LocalDateTime currentDttm) {
        return isPublicRound(slug, performanceRoundId)
                && lookupRound(performanceRoundId)
                .isReservationOpenAt(currentDttm);
    }

    public boolean isViewerCancellationOpen(Long performanceRoundId) {
        return lookupRound(performanceRoundId)
                .isViewerCancellationOpen();
    }

    public boolean isEntryOpen(
            Long actorMemberId, Long performanceRoundId) {
        validateAdmin(actorMemberId);
        return lookupRound(performanceRoundId).isEntryOpen();
    }

    private Map<Long, List<PerformanceRoundAccessibilityResponse>>
            groupAccessibilities(Long performanceProjectId) {
        Map<Long, List<PerformanceRoundAccessibilityResponse>> result =
                new HashMap<>();
        for (PerformanceRoundAccessibilityResponse accessibility
                : roundMapper.searchAccessibilitiesByProject(
                        performanceProjectId)) {
            result.computeIfAbsent(accessibility.performanceRoundId(),
                    ignored -> new ArrayList<>()).add(accessibility);
        }
        return result;
    }

    private PerformanceRound lockRound(Long performanceRoundId) {
        return roundMapper.lookupRoundForUpdate(performanceRoundId)
                .orElseThrow(() -> new PerformanceContentNotFoundException(
                        "performanceRoundId=" + performanceRoundId));
    }

    private PerformanceRound lookupRound(Long performanceRoundId) {
        return roundMapper.lookupRoundById(performanceRoundId)
                .orElseThrow(() -> new PerformanceContentNotFoundException(
                        "performanceRoundId=" + performanceRoundId));
    }

    private PerformanceRoundAccessibility lockAccessibility(
            Long performanceRoundAccessibilityId) {
        return roundMapper.lookupAccessibilityForUpdate(
                        performanceRoundAccessibilityId)
                .orElseThrow(() -> new PerformanceContentNotFoundException(
                        "performanceRoundAccessibilityId="
                                + performanceRoundAccessibilityId));
    }

    private void validateAdmin(Long actorMemberId) {
        MemberAccessContext access = memberService
                .lookupAccessContext(actorMemberId);
        if (!access.canManageGlobal()) {
            throw new PerformanceAccessDeniedException();
        }
    }

    private void validateInternal(Long actorMemberId) {
        MemberAccessContext access = memberService
                .lookupAccessContext(actorMemberId);
        if (!access.canReadInternal()) {
            throw new PerformanceAccessDeniedException();
        }
    }
}
