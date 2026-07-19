package kr.ac.tukorea.bandi.domain.member.controller;

import kr.ac.tukorea.bandi.domain.member.dto.request.CohortChangeRequest;
import kr.ac.tukorea.bandi.domain.member.dto.request.MemberPreRegisterRequest;
import kr.ac.tukorea.bandi.domain.member.dto.request.MemberSearchFilter;
import kr.ac.tukorea.bandi.domain.member.dto.request.MemberSearchCondition;
import kr.ac.tukorea.bandi.domain.member.dto.request.RoleChangeRequest;
import kr.ac.tukorea.bandi.domain.member.dto.request.StatusChangeRequest;
import kr.ac.tukorea.bandi.domain.member.dto.request.TeamChangeRequest;
import kr.ac.tukorea.bandi.domain.member.dto.response.CohortResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.MemberCreatedResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.MemberHistoryResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.MemberResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.TeamResponse;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.MemberApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberApiController implements MemberApiDocs {

    private final MemberService memberService;

    @Override
    public ResponseEntity<MemberResponse> lookupLoginMember(
            @LoginMember Long memberId) {
        return ResponseEntity.ok(memberService.lookupMember(memberId));
    }

    @Override
    public ResponseEntity<List<MemberResponse>> searchMembers(
            String keyword,
            Long teamId,
            MemberSearchFilter filter) {
        return ResponseEntity.ok(memberService.searchMembers(
                new MemberSearchCondition(keyword, teamId, filter.status(),
                        filter.role(), filter.ssoLinkStatus())));
    }

    @Override
    public ResponseEntity<MemberResponse> lookupMember(Long memberId) {
        return ResponseEntity.ok(memberService.lookupMember(memberId));
    }

    @Override
    public ResponseEntity<MemberHistoryResponse> lookupMemberHistory(
            Long memberId) {
        return ResponseEntity.ok(
                memberService.lookupMemberHistory(memberId));
    }

    @Override
    public ResponseEntity<MemberCreatedResponse> preRegister(
            @LoginMember Long actorMemberId,
            MemberPreRegisterRequest request) {
        Long memberId = memberService.preRegister(actorMemberId,
                request.toParam());
        return ResponseEntity.created(URI.create("/api/members/" + memberId))
                .body(new MemberCreatedResponse(memberId));
    }

    @Override
    public ResponseEntity<Void> changeTeam(
            @LoginMember Long actorMemberId,
            Long memberId,
            TeamChangeRequest request) {
        memberService.changeTeam(actorMemberId, request.toParam(memberId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> changeCohort(
            @LoginMember Long actorMemberId,
            Long memberId,
            CohortChangeRequest request) {
        memberService.changeCohort(actorMemberId, request.toParam(memberId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> changeRole(
            @LoginMember Long actorMemberId,
            Long memberId,
            RoleChangeRequest request) {
        memberService.changeRole(actorMemberId, request.toParam(memberId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> changeStatus(
            @LoginMember Long actorMemberId,
            Long memberId,
            StatusChangeRequest request) {
        memberService.changeStatus(actorMemberId, request.toParam(memberId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<TeamResponse>> searchTeams(
            boolean activeOnly) {
        return ResponseEntity.ok(memberService.searchTeams(activeOnly));
    }

    @Override
    public ResponseEntity<List<CohortResponse>> searchCohorts(
            boolean activeOnly) {
        return ResponseEntity.ok(memberService.searchCohorts(activeOnly));
    }
}
