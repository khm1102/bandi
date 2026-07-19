package kr.ac.tukorea.bandi.domain.production.controller;

import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskCreateRequest;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskFilter;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskSearchCondition;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskStatusParam;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskStatusRequest;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskUpdateRequest;
import kr.ac.tukorea.bandi.domain.production.dto.response.ProductionProgressResponse;
import kr.ac.tukorea.bandi.domain.production.dto.response.ProductionTaskCreatedResponse;
import kr.ac.tukorea.bandi.domain.production.dto.response.ProductionTaskHistoryResponse;
import kr.ac.tukorea.bandi.domain.production.dto.response.ProductionTaskResponse;
import kr.ac.tukorea.bandi.domain.production.service.ProductionTaskService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.ProductionTaskApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductionTaskApiController implements ProductionTaskApiDocs {
    private final ProductionTaskService productionTaskService;

    @Override
    public ResponseEntity<List<ProductionTaskResponse>> search(
            @LoginMember Long actorMemberId, Long performanceProjectId,
            Long teamId, ProductionTaskFilter filter, boolean overdueOnly,
            int offset, int limit) {
        return ResponseEntity.ok(productionTaskService.search(actorMemberId,
                new ProductionTaskSearchCondition(performanceProjectId, teamId,
                        filter.status(), overdueOnly, offset, limit)));
    }

    @Override
    public ResponseEntity<ProductionTaskCreatedResponse> create(
            @LoginMember Long actorMemberId, ProductionTaskCreateRequest request) {
        Long id = productionTaskService.create(actorMemberId, request.toParam());
        return ResponseEntity.created(URI.create("/api/production-tasks/" + id))
                .body(new ProductionTaskCreatedResponse(id));
    }

    @Override
    public ResponseEntity<Void> update(@LoginMember Long actorMemberId,
                                       Long taskId,
                                       ProductionTaskUpdateRequest request) {
        productionTaskService.update(actorMemberId, request.toParam(taskId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> changeStatus(@LoginMember Long actorMemberId,
                                             Long taskId,
                                             ProductionTaskStatusRequest request) {
        productionTaskService.changeStatus(actorMemberId,
                new ProductionTaskStatusParam(taskId, request.status(),
                        request.blockedReason(), request.comment()));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> delete(@LoginMember Long actorMemberId,
                                       Long taskId) {
        productionTaskService.delete(actorMemberId, taskId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ProductionProgressResponse> lookupProgress(
            @LoginMember Long actorMemberId, Long projectId) {
        return ResponseEntity.ok(productionTaskService.lookupProjectProgress(
                actorMemberId, projectId));
    }

    @Override
    public ResponseEntity<List<ProductionProgressResponse>> searchTeamProgress(
            @LoginMember Long actorMemberId, Long projectId) {
        return ResponseEntity.ok(productionTaskService.searchTeamProgress(
                actorMemberId, projectId));
    }

    @Override
    public ResponseEntity<List<ProductionTaskHistoryResponse>> searchHistories(
            @LoginMember Long actorMemberId, Long taskId) {
        return ResponseEntity.ok(productionTaskService.searchHistories(
                actorMemberId, taskId));
    }
}
