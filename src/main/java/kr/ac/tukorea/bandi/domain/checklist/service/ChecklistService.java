package kr.ac.tukorea.bandi.domain.checklist.service;

import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistCompletionParam;
import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistItemCreateParam;
import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistItemSearchCondition;
import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistItemUpdateParam;
import kr.ac.tukorea.bandi.domain.checklist.dto.response.ChecklistItemHistoryResponse;
import kr.ac.tukorea.bandi.domain.checklist.dto.response.ChecklistItemResponse;
import kr.ac.tukorea.bandi.domain.checklist.exception.ChecklistAccessDeniedException;
import kr.ac.tukorea.bandi.domain.checklist.exception.ChecklistItemNotFoundException;
import kr.ac.tukorea.bandi.domain.checklist.mapper.ChecklistMapper;
import kr.ac.tukorea.bandi.domain.checklist.model.ChecklistItem;
import kr.ac.tukorea.bandi.domain.checklist.model.ChecklistItemHistory;
import kr.ac.tukorea.bandi.domain.checklist.model.ChecklistScope;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceProjectService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChecklistService {

    private final ChecklistMapper checklistMapper;
    private final MemberService memberService;
    private final PerformanceProjectService projectService;
    private final PerformanceRoundService roundService;
    private final Clock clock;

    @Transactional
    public Long create(Long actorMemberId,
                       ChecklistItemCreateParam param) {
        MemberAccessContext access = lookupInternalAccess(actorMemberId);
        validateManagement(access, param.teamId());
        ChecklistItem item = ChecklistItem.create(
                param.performanceProjectId(), param.performanceRoundId(),
                param.teamId(), param.scope(), param.content(),
                param.required(), param.displayOrder(), actorMemberId);
        memberService.validateActiveTeam(param.teamId());
        projectService.validateProductionMutable(
                actorMemberId, param.performanceProjectId());
        validateRound(actorMemberId, item);
        checklistMapper.insert(item);
        return item.getChecklistItemId();
    }

    @Transactional
    public void update(Long actorMemberId,
                       ChecklistItemUpdateParam param) {
        MemberAccessContext access = lookupInternalAccess(actorMemberId);
        ChecklistItem current = lock(param.checklistItemId());
        validateManagement(access, current.getTeamId());
        projectService.validateProductionMutable(
                actorMemberId, current.getPerformanceProjectId());
        checklistMapper.update(current.edit(param.content(),
                param.required(), param.displayOrder(), actorMemberId));
    }

    @Transactional
    public void changeCompleted(Long actorMemberId,
                                ChecklistCompletionParam param) {
        MemberAccessContext access = lookupInternalAccess(actorMemberId);
        ChecklistItem current = lock(param.checklistItemId());
        validateContribution(access, current.getTeamId());
        projectService.validateProductionMutable(
                actorMemberId, current.getPerformanceProjectId());
        LocalDateTime changedDttm = now();
        ChecklistItem changed = current.changeCompleted(
                param.completed(), actorMemberId, changedDttm);
        checklistMapper.update(changed);
        checklistMapper.insertHistory(ChecklistItemHistory.change(
                current.getChecklistItemId(), current.isCompleted(),
                changed.isCompleted(), actorMemberId, changedDttm,
                param.reason()));
    }

    @Transactional
    public void delete(Long actorMemberId, Long checklistItemId) {
        MemberAccessContext access = lookupInternalAccess(actorMemberId);
        ChecklistItem current = lock(checklistItemId);
        validateManagement(access, current.getTeamId());
        projectService.validateProductionMutable(
                actorMemberId, current.getPerformanceProjectId());
        checklistMapper.delete(checklistItemId, actorMemberId, now());
    }

    public List<ChecklistItemResponse> search(
            Long actorMemberId, ChecklistItemSearchCondition condition) {
        lookupInternalAccess(actorMemberId);
        return checklistMapper.search(condition);
    }

    public List<ChecklistItemHistoryResponse> searchHistories(
            Long actorMemberId, Long checklistItemId) {
        lookupInternalAccess(actorMemberId);
        return checklistMapper.searchHistories(checklistItemId);
    }

    private ChecklistItem lock(Long checklistItemId) {
        return checklistMapper.lookupByIdForUpdate(checklistItemId)
                .orElseThrow(() -> new ChecklistItemNotFoundException(
                        checklistItemId));
    }

    private void validateRound(Long actorMemberId, ChecklistItem item) {
        if (item.getScope() == ChecklistScope.ROUND) {
            roundService.validateExists(actorMemberId,
                    item.getPerformanceRoundId(),
                    item.getPerformanceProjectId());
        }
    }

    private MemberAccessContext lookupInternalAccess(Long actorMemberId) {
        MemberAccessContext access = memberService
                .lookupAccessContext(actorMemberId);
        if (!access.canReadInternal()) {
            throw new ChecklistAccessDeniedException();
        }
        return access;
    }

    private void validateManagement(MemberAccessContext access,
                                    Long teamId) {
        if (!access.canManageTeam(teamId)) {
            throw new ChecklistAccessDeniedException();
        }
    }

    private void validateContribution(MemberAccessContext access,
                                      Long teamId) {
        if (!access.canContributeToTeam(teamId)) {
            throw new ChecklistAccessDeniedException();
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
