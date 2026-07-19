package kr.ac.tukorea.bandi.domain.production.service;

import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceProjectService;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskCreateParam;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskSearchCondition;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskStatusParam;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskUpdateParam;
import kr.ac.tukorea.bandi.domain.production.dto.response.ProductionProgressResponse;
import kr.ac.tukorea.bandi.domain.production.dto.response.ProductionTaskHistoryResponse;
import kr.ac.tukorea.bandi.domain.production.dto.response.ProductionTaskResponse;
import kr.ac.tukorea.bandi.domain.production.exception.ProductionAccessDeniedException;
import kr.ac.tukorea.bandi.domain.production.exception.ProductionTaskNotFoundException;
import kr.ac.tukorea.bandi.domain.production.mapper.ProductionTaskMapper;
import kr.ac.tukorea.bandi.domain.production.model.ProductionTask;
import kr.ac.tukorea.bandi.domain.production.model.ProductionTaskHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductionTaskService {

    private final ProductionTaskMapper productionTaskMapper;
    private final MemberService memberService;
    private final PerformanceProjectService performanceProjectService;
    private final Clock clock;

    @Transactional
    public Long create(Long actorMemberId, ProductionTaskCreateParam param) {
        MemberAccessContext access = lookupInternalAccess(actorMemberId);
        validateContribution(access, param.teamId());
        memberService.validateActiveTeam(param.teamId());
        performanceProjectService.validateProductionMutable(
                actorMemberId, param.performanceProjectId());
        ProductionTask task = ProductionTask.todo(
                param.performanceProjectId(), param.teamId(), param.title(),
                param.description(), param.startDate(), param.dueDate(),
                actorMemberId);
        productionTaskMapper.insert(task);
        log.info("제작 업무 생성 - productionTaskId={}, performanceProjectId={}, teamId={}",
                task.getProductionTaskId(), param.performanceProjectId(),
                param.teamId());
        return task.getProductionTaskId();
    }

    @Transactional
    public void update(Long actorMemberId, ProductionTaskUpdateParam param) {
        MemberAccessContext access = lookupInternalAccess(actorMemberId);
        ProductionTask task = lock(param.productionTaskId());
        validateManagement(access, task.getTeamId());
        performanceProjectService.validateProductionMutable(
                actorMemberId, task.getPerformanceProjectId());
        productionTaskMapper.update(task.edit(param.title(),
                param.description(), param.startDate(), param.dueDate(),
                actorMemberId));
    }

    @Transactional
    public void changeStatus(Long actorMemberId,
                             ProductionTaskStatusParam param) {
        MemberAccessContext access = lookupInternalAccess(actorMemberId);
        ProductionTask task = lock(param.productionTaskId());
        validateContribution(access, task.getTeamId());
        performanceProjectService.validateProductionMutable(
                actorMemberId, task.getPerformanceProjectId());
        ProductionTask changed = task.changeStatus(
                param.status(), param.blockedReason(), actorMemberId);
        LocalDateTime currentDttm = now();
        productionTaskMapper.update(changed);
        productionTaskMapper.insertHistory(ProductionTaskHistory.change(
                task.getProductionTaskId(), task.getStatus(),
                changed.getStatus(), param.comment(), actorMemberId,
                currentDttm));
        log.info("제작 업무 상태 변경 - productionTaskId={}, status={}, actorMemberId={}",
                task.getProductionTaskId(), changed.getStatus(), actorMemberId);
    }

    @Transactional
    public void delete(Long actorMemberId, Long productionTaskId) {
        MemberAccessContext access = lookupInternalAccess(actorMemberId);
        ProductionTask task = lock(productionTaskId);
        validateManagement(access, task.getTeamId());
        performanceProjectService.validateProductionMutable(
                actorMemberId, task.getPerformanceProjectId());
        productionTaskMapper.delete(
                productionTaskId, actorMemberId, now());
    }

    public List<ProductionTaskResponse> search(
            Long actorMemberId, ProductionTaskSearchCondition condition) {
        lookupInternalAccess(actorMemberId);
        return productionTaskMapper.search(condition, currentDate());
    }

    public ProductionProgressResponse lookupProjectProgress(
            Long actorMemberId, Long performanceProjectId) {
        lookupInternalAccess(actorMemberId);
        return productionTaskMapper.lookupProjectProgress(
                performanceProjectId, currentDate());
    }

    public List<ProductionProgressResponse> searchTeamProgress(
            Long actorMemberId, Long performanceProjectId) {
        lookupInternalAccess(actorMemberId);
        return productionTaskMapper.searchTeamProgress(
                performanceProjectId, currentDate());
    }

    public List<ProductionTaskHistoryResponse> searchHistories(
            Long actorMemberId, Long productionTaskId) {
        lookupInternalAccess(actorMemberId);
        return productionTaskMapper.searchHistories(productionTaskId);
    }

    private ProductionTask lock(Long productionTaskId) {
        return productionTaskMapper.lookupByIdForUpdate(productionTaskId)
                .orElseThrow(() -> new ProductionTaskNotFoundException(
                        productionTaskId));
    }

    private MemberAccessContext lookupInternalAccess(Long actorMemberId) {
        MemberAccessContext access = memberService
                .lookupAccessContext(actorMemberId);
        if (!access.canReadInternal()) {
            throw new ProductionAccessDeniedException();
        }
        return access;
    }

    private void validateContribution(MemberAccessContext access,
                                      Long teamId) {
        if (!access.canContributeToTeam(teamId)) {
            throw new ProductionAccessDeniedException();
        }
    }

    private void validateManagement(MemberAccessContext access,
                                    Long teamId) {
        if (!access.canManageTeam(teamId)) {
            throw new ProductionAccessDeniedException();
        }
    }

    private LocalDate currentDate() {
        return LocalDate.now(clock);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
