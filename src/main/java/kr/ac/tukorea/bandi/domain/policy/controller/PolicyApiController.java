package kr.ac.tukorea.bandi.domain.policy.controller;

import kr.ac.tukorea.bandi.domain.policy.dto.request.PolicyActiveRequest;
import kr.ac.tukorea.bandi.domain.policy.dto.request.PolicyDocumentRequest;
import kr.ac.tukorea.bandi.domain.policy.dto.request.PolicyVersionRequest;
import kr.ac.tukorea.bandi.domain.policy.dto.response.PolicyDocumentResponse;
import kr.ac.tukorea.bandi.domain.policy.dto.response.PolicyIdentifierResponse;
import kr.ac.tukorea.bandi.domain.policy.dto.response.PolicyVersionResponse;
import kr.ac.tukorea.bandi.domain.policy.service.PolicyService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.PolicyApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PolicyApiController implements PolicyApiDocs {

    private final PolicyService policyService;

    @Override
    public ResponseEntity<List<PolicyDocumentResponse>> searchDocuments(
            @LoginMember Long actorMemberId) {
        return ResponseEntity.ok(policyService.searchDocuments(actorMemberId));
    }

    @Override
    public ResponseEntity<PolicyIdentifierResponse> createDocument(
            @LoginMember Long actorMemberId, PolicyDocumentRequest request) {
        Long id = policyService.createDocument(actorMemberId,
                request.toParam());
        return created("/api/policies/" + id, id);
    }

    @Override
    public ResponseEntity<Void> changeActive(
            @LoginMember Long actorMemberId, Long policyDocumentId,
            PolicyActiveRequest request) {
        policyService.changeActive(actorMemberId, policyDocumentId,
                request.active());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<PolicyVersionResponse>> searchVersions(
            @LoginMember Long actorMemberId, Long policyDocumentId) {
        return ResponseEntity.ok(policyService.searchVersions(
                actorMemberId, policyDocumentId));
    }

    @Override
    public ResponseEntity<PolicyIdentifierResponse> publishVersion(
            @LoginMember Long actorMemberId, Long policyDocumentId,
            PolicyVersionRequest request) {
        Long id = policyService.publishVersion(actorMemberId,
                request.toParam(policyDocumentId));
        return created("/api/policies/" + policyDocumentId
                + "/versions/" + id, id);
    }

    private ResponseEntity<PolicyIdentifierResponse> created(
            String location, Long id) {
        return ResponseEntity.created(URI.create(location))
                .body(new PolicyIdentifierResponse(id));
    }
}
