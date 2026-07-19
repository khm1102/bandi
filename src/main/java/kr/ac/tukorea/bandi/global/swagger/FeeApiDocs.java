package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.tukorea.bandi.domain.fee.dto.response.MemberFeeResponse;
import kr.ac.tukorea.bandi.domain.fee.dto.response.MemberFeeSummaryResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/fees")
@Tag(name = ApiTag.FEE, description = "내 회비 부과와 납부 상태 조회 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface FeeApiDocs {

    @Operation(summary = "내 회비 목록 조회")
    @GetMapping("/mine")
    ResponseEntity<List<MemberFeeResponse>> searchMine(
            @Parameter(hidden = true) @LoginMember Long actorMemberId);

    @Operation(summary = "내 회비 요약 조회")
    @GetMapping("/mine/summary")
    ResponseEntity<MemberFeeSummaryResponse> lookupMySummary(
            @Parameter(hidden = true) @LoginMember Long actorMemberId);
}
