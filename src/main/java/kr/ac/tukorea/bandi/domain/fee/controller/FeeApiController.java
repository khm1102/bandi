package kr.ac.tukorea.bandi.domain.fee.controller;

import kr.ac.tukorea.bandi.domain.fee.dto.response.MemberFeeResponse;
import kr.ac.tukorea.bandi.domain.fee.dto.response.MemberFeeSummaryResponse;
import kr.ac.tukorea.bandi.domain.fee.service.FeeService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.FeeApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FeeApiController implements FeeApiDocs {

    private final FeeService feeService;

    @Override
    public ResponseEntity<List<MemberFeeResponse>> searchMine(
            @LoginMember Long actorMemberId) {
        return ResponseEntity.ok(feeService.searchMyFees(actorMemberId));
    }

    @Override
    public ResponseEntity<MemberFeeSummaryResponse> lookupMySummary(
            @LoginMember Long actorMemberId) {
        return ResponseEntity.ok(feeService.lookupMySummary(actorMemberId));
    }
}
