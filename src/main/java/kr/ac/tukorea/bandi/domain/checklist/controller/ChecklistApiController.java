package kr.ac.tukorea.bandi.domain.checklist.controller;

import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistCompletionParam;
import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistCompletionRequest;
import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistItemCreateRequest;
import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistItemSearchCondition;
import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistItemUpdateRequest;
import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistSearchFilter;
import kr.ac.tukorea.bandi.domain.checklist.dto.response.ChecklistItemCreatedResponse;
import kr.ac.tukorea.bandi.domain.checklist.dto.response.ChecklistItemHistoryResponse;
import kr.ac.tukorea.bandi.domain.checklist.dto.response.ChecklistItemResponse;
import kr.ac.tukorea.bandi.domain.checklist.service.ChecklistService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.ChecklistApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChecklistApiController implements ChecklistApiDocs {
    private final ChecklistService checklistService;

    @Override
    public ResponseEntity<List<ChecklistItemResponse>> search(
            @LoginMember Long actorMemberId, Long performanceProjectId,
            Long performanceRoundId, Long teamId, ChecklistSearchFilter filter) {
        return ResponseEntity.ok(checklistService.search(actorMemberId,
                new ChecklistItemSearchCondition(performanceProjectId,
                        performanceRoundId, teamId, filter.scope())));
    }

    @Override
    public ResponseEntity<ChecklistItemCreatedResponse> create(
            @LoginMember Long actorMemberId, ChecklistItemCreateRequest request) {
        Long id = checklistService.create(actorMemberId, request.toParam());
        return ResponseEntity.created(URI.create("/api/checklist-items/" + id))
                .body(new ChecklistItemCreatedResponse(id));
    }

    @Override
    public ResponseEntity<Void> update(@LoginMember Long actorMemberId,
                                       Long itemId,
                                       ChecklistItemUpdateRequest request) {
        checklistService.update(actorMemberId, request.toParam(itemId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> changeCompletion(
            @LoginMember Long actorMemberId, Long itemId,
            ChecklistCompletionRequest request) {
        checklistService.changeCompleted(actorMemberId,
                new ChecklistCompletionParam(itemId, request.completed(),
                        request.reason()));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> delete(@LoginMember Long actorMemberId,
                                       Long itemId) {
        checklistService.delete(actorMemberId, itemId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<ChecklistItemHistoryResponse>> searchHistories(
            @LoginMember Long actorMemberId, Long itemId) {
        return ResponseEntity.ok(checklistService.searchHistories(actorMemberId,
                itemId));
    }
}
