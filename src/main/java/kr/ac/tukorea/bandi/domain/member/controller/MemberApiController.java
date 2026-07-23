package kr.ac.tukorea.bandi.domain.member.controller;

import kr.ac.tukorea.bandi.domain.member.dto.request.CohortChangeRequest;
import kr.ac.tukorea.bandi.domain.member.dto.request.MemberPreRegisterRequest;
import kr.ac.tukorea.bandi.domain.member.dto.request.MemberPageSearchParam;
import kr.ac.tukorea.bandi.domain.member.dto.request.MemberSearchFilter;
import kr.ac.tukorea.bandi.domain.member.dto.request.MemberSearchCondition;
import kr.ac.tukorea.bandi.domain.member.dto.request.RoleChangeRequest;
import kr.ac.tukorea.bandi.domain.member.dto.request.StatusChangeRequest;
import kr.ac.tukorea.bandi.domain.member.dto.request.TeamChangeRequest;
import kr.ac.tukorea.bandi.domain.member.dto.response.CohortResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.MemberCreatedResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.MemberHistoryResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.MemberProfileResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.MemberResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.MemberStatsResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.TeamMemberResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.TeamResponse;
import kr.ac.tukorea.bandi.domain.member.service.MemberProfileService;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.file.service.FileUploadParam;
import kr.ac.tukorea.bandi.global.response.FileDownloadResponse;
import kr.ac.tukorea.bandi.global.response.PageResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.MemberApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberApiController implements MemberApiDocs {

    private final MemberService memberService;
    private final MemberProfileService memberProfileService;

    @Override
    public ResponseEntity<MemberResponse> lookupLoginMember(
            @LoginMember Long memberId) {
        return ResponseEntity.ok(memberService.lookupMember(memberId));
    }

    @Override
    public ResponseEntity<MemberProfileResponse> lookupLoginMemberProfile(
            @LoginMember Long memberId) {
        return ResponseEntity.ok(memberProfileService.lookupProfile(memberId));
    }

    @Override
    public ResponseEntity<MemberProfileResponse> uploadLoginMemberProfilePhoto(
            @LoginMember Long memberId, MultipartFile file) {
        MemberProfileResponse response = memberProfileService.uploadProfilePhoto(memberId,
                new FileUploadParam("member-profile", file.getOriginalFilename(), file.getSize(),
                        file::getInputStream, memberId));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteLoginMemberProfilePhoto(
            @LoginMember Long memberId) {
        memberProfileService.deleteProfilePhoto(memberId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Resource> openProfilePhoto(
            @LoginMember Long requesterMemberId, Long memberId) {
        FileDownloadResponse download = memberProfileService.openProfilePhoto(
                requesterMemberId, memberId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .contentLength(download.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(download.originalName()).build().toString())
                .body(download.resource());
    }

    @Override
    public ResponseEntity<PageResponse<TeamMemberResponse>> searchTeamMembers(
            @LoginMember Long memberId, String keyword, Long teamId,
            MemberSearchFilter filter, int page, int pageSize) {
        return ResponseEntity.ok(memberProfileService.searchTeamMembers(memberId,
                new MemberPageSearchParam(keyword, teamId, null, filter.status(),
                        null, null, page, pageSize)));
    }

    @Override
    public ResponseEntity<PageResponse<MemberResponse>> searchMembers(
            String keyword,
            Long teamId,
            Long cohortId,
            MemberSearchFilter filter,
            int page,
            int pageSize) {
        return ResponseEntity.ok(memberService.searchMemberPage(
                new MemberPageSearchParam(keyword, teamId, cohortId, filter.status(),
                        filter.role(), filter.ssoLinkStatus(), page, pageSize)));
    }

    @Override
    public ResponseEntity<MemberStatsResponse> lookupMemberStats() {
        return ResponseEntity.ok(memberService.lookupMemberStats());
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
