package kr.ac.tukorea.bandi.domain.performance.controller;

import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceProjectFilter;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceProjectRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceProjectSearchCondition;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceProjectStatusParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceProjectStatusRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundStatusParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundStatusRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.RoundAccessibilityRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceIdentifierResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceProjectResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceRoundAccessibilityResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceRoundResponse;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceProjectService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.PerformanceCoreApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PerformanceCoreApiController implements PerformanceCoreApiDocs {

    private final PerformanceProjectService projectService;
    private final PerformanceRoundService roundService;

    @Override
    public ResponseEntity<List<PerformanceProjectResponse>> searchProjects(
            @LoginMember Long actorMemberId, Short academicYear, String termCode,
            PerformanceProjectFilter filter, int offset, int limit) {
        return ResponseEntity.ok(projectService.search(actorMemberId,
                new PerformanceProjectSearchCondition(academicYear, termCode,
                        filter.status(), offset, limit)));
    }

    @Override
    public ResponseEntity<PerformanceIdentifierResponse> createProject(
            @LoginMember Long actorMemberId, PerformanceProjectRequest request) {
        Long id = projectService.create(actorMemberId, request.toCreateParam());
        return ResponseEntity.created(URI.create(
                        "/api/performance-management/projects/" + id))
                .body(new PerformanceIdentifierResponse(id));
    }

    @Override
    public ResponseEntity<Void> updateProject(@LoginMember Long actorMemberId,
                                              Long projectId,
                                              PerformanceProjectRequest request) {
        projectService.update(actorMemberId, request.toUpdateParam(projectId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> changeProjectStatus(
            @LoginMember Long actorMemberId, Long projectId,
            PerformanceProjectStatusRequest request) {
        projectService.changeStatus(actorMemberId,
                new PerformanceProjectStatusParam(projectId, request.status()));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<PerformanceRoundResponse>> searchRounds(
            @LoginMember Long actorMemberId, Long projectId) {
        return ResponseEntity.ok(roundService.searchRounds(actorMemberId, projectId));
    }

    @Override
    public ResponseEntity<PerformanceIdentifierResponse> createRound(
            @LoginMember Long actorMemberId, PerformanceRoundRequest request) {
        Long id = roundService.createRound(actorMemberId, request.toParam(null));
        return ResponseEntity.created(URI.create(
                        "/api/performance-management/rounds/" + id))
                .body(new PerformanceIdentifierResponse(id));
    }

    @Override
    public ResponseEntity<Void> updateRound(@LoginMember Long actorMemberId,
                                            Long roundId,
                                            PerformanceRoundRequest request) {
        roundService.updateRound(actorMemberId, request.toParam(roundId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> changeRoundStatus(
            @LoginMember Long actorMemberId, Long roundId,
            PerformanceRoundStatusRequest request) {
        roundService.changeRoundStatus(actorMemberId,
                new PerformanceRoundStatusParam(roundId, request.status()));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<PerformanceRoundAccessibilityResponse>>
            searchAccessibilities(@LoginMember Long actorMemberId, Long roundId) {
        return ResponseEntity.ok(roundService.searchAccessibilities(actorMemberId,
                roundId));
    }

    @Override
    public ResponseEntity<PerformanceIdentifierResponse> createAccessibility(
            @LoginMember Long actorMemberId, Long roundId,
            RoundAccessibilityRequest request) {
        Long id = roundService.createAccessibility(actorMemberId,
                request.toParam(null, roundId));
        return ResponseEntity.created(URI.create(
                        "/api/performance-management/accessibilities/" + id))
                .body(new PerformanceIdentifierResponse(id));
    }

    @Override
    public ResponseEntity<Void> updateAccessibility(
            @LoginMember Long actorMemberId, Long roundId, Long accessibilityId,
            RoundAccessibilityRequest request) {
        roundService.updateAccessibility(actorMemberId,
                request.toParam(accessibilityId, roundId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> removeAccessibility(
            @LoginMember Long actorMemberId, Long accessibilityId) {
        roundService.removeAccessibility(actorMemberId, accessibilityId);
        return ResponseEntity.noContent().build();
    }
}
