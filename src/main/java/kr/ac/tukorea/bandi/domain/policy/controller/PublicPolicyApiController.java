package kr.ac.tukorea.bandi.domain.policy.controller;

import kr.ac.tukorea.bandi.domain.policy.dto.response.PolicyVersionResponse;
import kr.ac.tukorea.bandi.domain.policy.service.PolicyService;
import kr.ac.tukorea.bandi.global.swagger.PublicPolicyApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PublicPolicyApiController implements PublicPolicyApiDocs {

    private final PolicyService policyService;

    @Override
    public ResponseEntity<PolicyVersionResponse> lookupReservationPrivacy() {
        return ResponseEntity.ok(
                policyService.lookupCurrentReservationPrivacy());
    }
}
