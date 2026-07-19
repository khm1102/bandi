package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.tukorea.bandi.domain.policy.dto.response.PolicyVersionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/public-policies")
@Tag(name = ApiTag.POLICY, description = "외부 공개 정책 조회 API")
public interface PublicPolicyApiDocs {

    @Operation(summary = "현재 관람 신청 개인정보 동의문 조회")
    @GetMapping("/reservation-privacy")
    ResponseEntity<PolicyVersionResponse> lookupReservationPrivacy();
}
