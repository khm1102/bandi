package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/fee-management")
@Tag(name = ApiTag.FEE, description = "회비 항목과 수납 상태 관리 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface FeeManagementApiDocs {

    @Operation(summary = "회비 항목 목록 조회")
    @GetMapping
    ResponseEntity<List<FeeItemResponse>> searchItems(
            @Parameter(hidden = true) @LoginMember Long actorMemberId);

    @Operation(summary = "회비 항목 등록")
    @PostMapping
    ResponseEntity<FeeItemCreatedResponse> create(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody FeeItemRequest request);

    @Operation(summary = "회비 항목 수정")
    @PutMapping("/{feeItemId}")
    ResponseEntity<Void> update(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long feeItemId,
            @Valid @RequestBody FeeItemRequest request);

    @Operation(summary = "회비 부과 대상 확정")
    @PostMapping("/{feeItemId}/open")
    ResponseEntity<FeeTargetsOpenedResponse> open(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long feeItemId,
            @Valid @RequestBody FeeOpenRequest request);

    @Operation(summary = "회비 부과 마감")
    @PostMapping("/{feeItemId}/close")
    ResponseEntity<Void> close(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long feeItemId);

    @Operation(summary = "회비 항목 취소")
    @PostMapping("/{feeItemId}/cancel")
    ResponseEntity<Void> cancel(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long feeItemId,
            @Valid @RequestBody FeeCancelRequest request);

    @Operation(summary = "멤버별 회비 수납 상태 일괄 처리")
    @PostMapping("/{feeItemId}/charges/process")
    ResponseEntity<FeeChargesProcessedResponse> processCharges(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long feeItemId,
            @Valid @RequestBody FeeChargeProcessRequest request);

    @Operation(summary = "회비 항목별 부과 명단 조회")
    @GetMapping("/{feeItemId}/charges")
    ResponseEntity<List<FeeChargeResponse>> searchCharges(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long feeItemId);

    @Operation(summary = "회비 수납 상태 변경 이력 조회")
    @GetMapping("/charges/{feeChargeId}/histories")
    ResponseEntity<List<FeeChargeHistoryResponse>> searchHistories(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long feeChargeId);
}
