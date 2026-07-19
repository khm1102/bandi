package kr.ac.tukorea.bandi.domain.fee.controller;

import kr.ac.tukorea.bandi.domain.fee.dto.request.FeeCancelRequest;
import kr.ac.tukorea.bandi.domain.fee.dto.request.FeeChargeProcessRequest;
import kr.ac.tukorea.bandi.domain.fee.dto.request.FeeItemRequest;
import kr.ac.tukorea.bandi.domain.fee.dto.request.FeeOpenRequest;
import kr.ac.tukorea.bandi.domain.fee.dto.response.FeeChargeHistoryResponse;
import kr.ac.tukorea.bandi.domain.fee.dto.response.FeeChargeResponse;
import kr.ac.tukorea.bandi.domain.fee.dto.response.FeeChargesProcessedResponse;
import kr.ac.tukorea.bandi.domain.fee.dto.response.FeeItemCreatedResponse;
import kr.ac.tukorea.bandi.domain.fee.dto.response.FeeItemResponse;
import kr.ac.tukorea.bandi.domain.fee.dto.response.FeeTargetsOpenedResponse;
import kr.ac.tukorea.bandi.domain.fee.service.FeeService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.FeeManagementApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class FeeManagementApiController implements FeeManagementApiDocs {

    private final FeeService feeService;

    @Override
    public ResponseEntity<List<FeeItemResponse>> searchItems(
            @LoginMember Long actorMemberId) {
        return ResponseEntity.ok(feeService.searchItems(actorMemberId));
    }

    @Override
    public ResponseEntity<FeeItemCreatedResponse> create(
            @LoginMember Long actorMemberId, FeeItemRequest request) {
        Long id = feeService.create(actorMemberId, request.toWriteParam());
        return ResponseEntity.created(URI.create("/api/fee-management/" + id))
                .body(new FeeItemCreatedResponse(id));
    }

    @Override
    public ResponseEntity<Void> update(@LoginMember Long actorMemberId,
                                       Long feeItemId, FeeItemRequest request) {
        feeService.update(actorMemberId, request.toUpdateParam(feeItemId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<FeeTargetsOpenedResponse> open(
            @LoginMember Long actorMemberId, Long feeItemId,
            FeeOpenRequest request) {
        int count = feeService.open(actorMemberId, request.toParam(feeItemId));
        return ResponseEntity.ok(new FeeTargetsOpenedResponse(count));
    }

    @Override
    public ResponseEntity<Void> close(@LoginMember Long actorMemberId,
                                      Long feeItemId) {
        feeService.close(actorMemberId, feeItemId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> cancel(@LoginMember Long actorMemberId,
                                       Long feeItemId, FeeCancelRequest request) {
        feeService.cancel(actorMemberId, feeItemId, request.reason());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<FeeChargesProcessedResponse> processCharges(
            @LoginMember Long actorMemberId, Long feeItemId,
            FeeChargeProcessRequest request) {
        int count = feeService.processCharges(actorMemberId,
                request.toParam(feeItemId));
        return ResponseEntity.ok(new FeeChargesProcessedResponse(count));
    }

    @Override
    public ResponseEntity<List<FeeChargeResponse>> searchCharges(
            @LoginMember Long actorMemberId, Long feeItemId) {
        return ResponseEntity.ok(feeService.searchCharges(actorMemberId, feeItemId));
    }

    @Override
    public ResponseEntity<List<FeeChargeHistoryResponse>> searchHistories(
            @LoginMember Long actorMemberId, Long feeChargeId) {
        return ResponseEntity.ok(feeService.searchChargeHistories(actorMemberId,
                feeChargeId));
    }
}
