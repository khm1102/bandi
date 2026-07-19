package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.tukorea.bandi.domain.member.dto.request.CohortChangeRequest;
import kr.ac.tukorea.bandi.domain.member.dto.request.MemberPreRegisterRequest;
import kr.ac.tukorea.bandi.domain.member.dto.request.RoleChangeRequest;
import kr.ac.tukorea.bandi.domain.member.dto.request.StatusChangeRequest;
import kr.ac.tukorea.bandi.domain.member.dto.request.TeamChangeRequest;
import kr.ac.tukorea.bandi.domain.member.dto.response.CohortResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.MemberCreatedResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.MemberHistoryResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.MemberResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.TeamResponse;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/api/members")
@Tag(name = ApiTag.MEMBER, description = "멤버 등록·조회·조직·권한 관리 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface MemberApiDocs {

    @Operation(summary = "멤버 목록 조회")
    @GetMapping
    ResponseEntity<List<MemberResponse>> searchMembers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) MemberStatus status,
            @RequestParam(required = false) ClubRole role,
            @RequestParam(required = false) SsoLinkStatus ssoLinkStatus);

    @Operation(summary = "멤버 상세 조회")
    @GetMapping("/{memberId}")
    ResponseEntity<MemberResponse> lookupMember(
            @Parameter(description = "멤버 식별자")
            @PathVariable Long memberId);

    @Operation(summary = "멤버 변경 이력 조회")
    @GetMapping("/{memberId}/histories")
    ResponseEntity<MemberHistoryResponse> lookupMemberHistory(
            @PathVariable Long memberId);

    @Operation(summary = "멤버 사전 등록")
    @PostMapping
    ResponseEntity<MemberCreatedResponse> preRegister(
            @LoginMember Long actorMemberId,
            @Valid @RequestBody MemberPreRegisterRequest request);

    @Operation(summary = "멤버 팀 변경")
    @PatchMapping("/{memberId}/team")
    ResponseEntity<Void> changeTeam(
            @LoginMember Long actorMemberId,
            @PathVariable Long memberId,
            @Valid @RequestBody TeamChangeRequest request);

    @Operation(summary = "멤버 기수 변경")
    @PatchMapping("/{memberId}/cohort")
    ResponseEntity<Void> changeCohort(
            @LoginMember Long actorMemberId,
            @PathVariable Long memberId,
            @Valid @RequestBody CohortChangeRequest request);

    @Operation(summary = "멤버 권한 변경")
    @PatchMapping("/{memberId}/role")
    ResponseEntity<Void> changeRole(
            @LoginMember Long actorMemberId,
            @PathVariable Long memberId,
            @Valid @RequestBody RoleChangeRequest request);

    @Operation(summary = "멤버 상태 변경")
    @PatchMapping("/{memberId}/status")
    ResponseEntity<Void> changeStatus(
            @LoginMember Long actorMemberId,
            @PathVariable Long memberId,
            @Valid @RequestBody StatusChangeRequest request);

    @Operation(summary = "팀 기준정보 조회")
    @GetMapping("/reference/teams")
    ResponseEntity<List<TeamResponse>> searchTeams(
            @RequestParam(defaultValue = "true") boolean activeOnly);

    @Operation(summary = "기수 기준정보 조회")
    @GetMapping("/reference/cohorts")
    ResponseEntity<List<CohortResponse>> searchCohorts(
            @RequestParam(defaultValue = "true") boolean activeOnly);
}
