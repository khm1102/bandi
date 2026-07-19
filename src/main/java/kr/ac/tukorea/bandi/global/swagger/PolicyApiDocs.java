package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.tukorea.bandi.domain.policy.dto.request.PolicyActiveRequest;
import kr.ac.tukorea.bandi.domain.policy.dto.request.PolicyDocumentRequest;
import kr.ac.tukorea.bandi.domain.policy.dto.request.PolicyVersionRequest;
import kr.ac.tukorea.bandi.domain.policy.dto.response.PolicyDocumentResponse;
import kr.ac.tukorea.bandi.domain.policy.dto.response.PolicyIdentifierResponse;
import kr.ac.tukorea.bandi.domain.policy.dto.response.PolicyVersionResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/policies")
@Tag(name = ApiTag.POLICY, description = "정책 문서·버전 관리 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface PolicyApiDocs {

    @Operation(summary = "정책 문서 목록 조회")
    @GetMapping
    ResponseEntity<List<PolicyDocumentResponse>> searchDocuments(
            @Parameter(hidden = true) @LoginMember Long actorMemberId);

    @Operation(summary = "정책 문서 생성")
    @PostMapping
    ResponseEntity<PolicyIdentifierResponse> createDocument(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody PolicyDocumentRequest request);

    @Operation(summary = "정책 문서 활성 상태 변경")
    @PatchMapping("/{policyDocumentId}/active")
    ResponseEntity<Void> changeActive(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long policyDocumentId,
            @Valid @RequestBody PolicyActiveRequest request);

    @Operation(summary = "정책 문서 버전 목록 조회")
    @GetMapping("/{policyDocumentId}/versions")
    ResponseEntity<List<PolicyVersionResponse>> searchVersions(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long policyDocumentId);

    @Operation(summary = "정책 문서 새 버전 게시")
    @PostMapping("/{policyDocumentId}/versions")
    ResponseEntity<PolicyIdentifierResponse> publishVersion(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long policyDocumentId,
            @Valid @RequestBody PolicyVersionRequest request);
}
