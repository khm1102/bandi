package kr.ac.tukorea.bandi.domain.member.service;

import kr.ac.tukorea.bandi.domain.file.model.StoredFile;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.file.service.FileUploadParam;
import kr.ac.tukorea.bandi.domain.file.service.ProfilePhotoRetirementService;
import kr.ac.tukorea.bandi.domain.member.dto.request.MemberSearchCondition;
import kr.ac.tukorea.bandi.domain.member.dto.response.MemberProfileResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.TeamMemberResponse;
import kr.ac.tukorea.bandi.domain.member.exception.CohortNotFoundException;
import kr.ac.tukorea.bandi.domain.member.exception.MemberManagementForbiddenException;
import kr.ac.tukorea.bandi.domain.member.exception.MemberNotFoundException;
import kr.ac.tukorea.bandi.domain.member.exception.TeamNotFoundException;
import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.Team;
import kr.ac.tukorea.bandi.global.response.FileDownloadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberProfileService {

    private final MemberMapper memberMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final FileService fileService;
    private final ProfilePhotoRetirementService retirementService;

    public MemberProfileResponse lookupProfile(Long memberId) {
        return toProfile(findMember(memberId));
    }

    public List<TeamMemberResponse> searchTeamMembers(Long actorMemberId) {
        Member actor = findMember(actorMemberId);
        MemberAccessContext access = MemberAccessContext.from(actor);
        if (!access.canManageGlobal() && !access.canManageTeam(actor.getTeamId())) {
            throw new MemberManagementForbiddenException(actorMemberId);
        }
        Long teamId = access.canManageGlobal() ? null : actor.getTeamId();
        return memberMapper.searchByCondition(new MemberSearchCondition(
                        null, teamId, null, null, null)).stream()
                .map(member -> TeamMemberResponse.from(member,
                        findTeam(member.getTeamId()).getName()))
                .toList();
    }

    @Transactional
    public MemberProfileResponse uploadProfilePhoto(Long actorMemberId,
                                                     FileUploadParam param) {
        Long storedFileId = fileService.uploadProfileImage(param);
        try {
            return assignProfilePhoto(actorMemberId, storedFileId);
        } catch (RuntimeException exception) {
            retirementService.queueOrphan(storedFileId);
            retirementService.processUncompleted();
            throw exception;
        }
    }

    @Transactional
    public void deleteProfilePhoto(Long actorMemberId) {
        Member member = lockMember(actorMemberId);
        if (member.getProfilePhotoFileId() == null) {
            return;
        }
        StoredFile oldPhoto = fileService.lookupProfileImageReadyOwnedBy(
                member.getProfilePhotoFileId(), actorMemberId);
        memberMapper.updateProfilePhoto(actorMemberId, null);
        retirementService.queue(oldPhoto);
    }

    public FileDownloadResponse openProfilePhoto(Long requesterMemberId,
                                                 Long targetMemberId) {
        ensureActiveMember(requesterMemberId);
        Member target = findMember(targetMemberId);
        if (target.getProfilePhotoFileId() == null) {
            throw new kr.ac.tukorea.bandi.domain.file.exception.StoredFileNotFoundException(null);
        }
        return fileService.openProfileImageDownload(target.getProfilePhotoFileId());
    }

    private MemberProfileResponse assignProfilePhoto(Long actorMemberId,
                                                      Long storedFileId) {
        Member member = lockMember(actorMemberId);
        StoredFile newPhoto = fileService.lookupProfileImageReadyOwnedBy(storedFileId,
                actorMemberId);
        memberMapper.updateProfilePhoto(actorMemberId, storedFileId);
        if (member.getProfilePhotoFileId() != null) {
            StoredFile oldPhoto = fileService.lookupProfileImageReadyOwnedBy(
                    member.getProfilePhotoFileId(), actorMemberId);
            retirementService.queue(oldPhoto);
        }
        return toProfile(member).withProfilePhoto(newPhoto.getStoredFileId() != null);
    }

    private MemberProfileResponse toProfile(Member member) {
        return MemberProfileResponse.from(member,
                findTeam(member.getTeamId()).getName(), findCohort(member.getCohortId()).getName());
    }

    private void ensureActiveMember(Long memberId) {
        if (!MemberAccessContext.from(findMember(memberId)).canReadInternal()) {
            throw new MemberManagementForbiddenException(memberId);
        }
    }

    private Member findMember(Long memberId) {
        return memberMapper.lookupById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    private Member lockMember(Long memberId) {
        return memberMapper.lookupByIdForUpdate(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    private Team findTeam(Long teamId) {
        return teamMapper.lookupById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));
    }

    private Cohort findCohort(Long cohortId) {
        return cohortMapper.lookupById(cohortId)
                .orElseThrow(() -> new CohortNotFoundException(cohortId));
    }
}
