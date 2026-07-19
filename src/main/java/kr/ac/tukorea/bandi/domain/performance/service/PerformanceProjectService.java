package kr.ac.tukorea.bandi.domain.performance.service;

import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceProjectCreateParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceProjectSearchCondition;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceProjectStatusParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceProjectUpdateParam;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceProjectResponse;
import kr.ac.tukorea.bandi.domain.performance.exception.DuplicatePerformanceTermException;
import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceProjectException;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceAccessDeniedException;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceProjectNotFoundException;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformanceProjectMapper;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceProjectService {

    private final PerformanceProjectMapper performanceProjectMapper;
    private final MemberService memberService;

    @Transactional
    public Long create(Long actorMemberId,
                       PerformanceProjectCreateParam param) {
        validateAdmin(actorMemberId);
        PerformanceProject project = PerformanceProject.planning(
                param.academicYear(), param.termCode(), param.title(),
                param.productionStartDate(), param.productionEndDate(),
                param.place(), actorMemberId);
        try {
            performanceProjectMapper.insert(project);
        } catch (DuplicateKeyException exception) {
            throw new DuplicatePerformanceTermException(
                    param.academicYear(), param.termCode());
        }
        log.info("공연 프로젝트 생성 - performanceProjectId={}, actorMemberId={}",
                project.getPerformanceProjectId(), actorMemberId);
        return project.getPerformanceProjectId();
    }

    @Transactional
    public void update(Long actorMemberId,
                       PerformanceProjectUpdateParam param) {
        validateAdmin(actorMemberId);
        PerformanceProject project = lock(param.performanceProjectId());
        PerformanceProject changed = project.edit(param.academicYear(),
                param.termCode(), param.title(), param.productionStartDate(),
                param.productionEndDate(), param.place(), actorMemberId);
        try {
            performanceProjectMapper.update(changed);
        } catch (DuplicateKeyException exception) {
            throw new DuplicatePerformanceTermException(
                    param.academicYear(), param.termCode());
        }
    }

    @Transactional
    public void changeStatus(Long actorMemberId,
                             PerformanceProjectStatusParam param) {
        validateAdmin(actorMemberId);
        PerformanceProject project = lock(param.performanceProjectId());
        PerformanceProject changed = project.changeStatus(
                param.status(), actorMemberId);
        performanceProjectMapper.update(changed);
        log.info("공연 프로젝트 상태 변경 - performanceProjectId={}, status={}, actorMemberId={}",
                param.performanceProjectId(), param.status(), actorMemberId);
    }

    public List<PerformanceProjectResponse> search(
            Long actorMemberId,
            PerformanceProjectSearchCondition condition) {
        validateInternal(actorMemberId);
        return performanceProjectMapper.search(condition);
    }

    public Optional<PerformanceProjectResponse> lookupCurrent(
            Long actorMemberId, short academicYear, String termCode) {
        validateInternal(actorMemberId);
        validateTerm(academicYear, termCode);
        return performanceProjectMapper.lookupCurrent(
                academicYear, termCode.strip());
    }

    private PerformanceProject lock(Long performanceProjectId) {
        return performanceProjectMapper
                .lookupByIdForUpdate(performanceProjectId)
                .orElseThrow(() -> new PerformanceProjectNotFoundException(
                        performanceProjectId));
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

    private void validateTerm(short academicYear, String termCode) {
        if (academicYear < 1 || termCode == null || termCode.isBlank()
                || termCode.strip().length() > 20) {
            throw new InvalidPerformanceProjectException("term");
        }
    }
}
