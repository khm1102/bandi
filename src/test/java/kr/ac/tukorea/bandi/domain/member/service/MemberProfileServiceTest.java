package kr.ac.tukorea.bandi.domain.member.service;

import kr.ac.tukorea.bandi.domain.file.model.StoredFile;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.file.service.ProfilePhotoRetirementService;
import kr.ac.tukorea.bandi.domain.member.dto.request.MemberSearchCondition;
import kr.ac.tukorea.bandi.domain.member.dto.response.MemberProfileResponse;
import kr.ac.tukorea.bandi.domain.member.exception.MemberManagementForbiddenException;
import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.AcademicStatus;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.domain.member.model.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberProfileServiceTest {

    private static final Long MEMBER_ID = 10L;
    private static final Long TEAM_ID = 20L;
    private static final Long COHORT_ID = 30L;
    private static final Long PROFILE_FILE_ID = 40L;

    @Mock
    private MemberMapper memberMapper;
    @Mock
    private TeamMapper teamMapper;
    @Mock
    private CohortMapper cohortMapper;
    @Mock
    private FileService fileService;
    @Mock
    private ProfilePhotoRetirementService retirementService;

    private MemberProfileService profileService;

    @BeforeEach
    void setUp() {
        profileService = new MemberProfileService(memberMapper, teamMapper, cohortMapper,
                fileService, retirementService);
    }

    @Test
    void 프로필을_조회하면_팀과_기수_표시명을_함께_반환한다() {
        given(memberMapper.lookupById(MEMBER_ID)).willReturn(Optional.of(member(
                MEMBER_ID, TEAM_ID, ClubRole.MEMBER, null)));
        given(teamMapper.lookupById(TEAM_ID)).willReturn(Optional.of(team(TEAM_ID, "무대팀")));
        given(cohortMapper.lookupById(COHORT_ID)).willReturn(Optional.of(cohort()));

        MemberProfileResponse result = profileService.lookupProfile(MEMBER_ID);

        assertThat(result.teamId()).isEqualTo(TEAM_ID);
        assertThat(result.teamName()).isEqualTo("무대팀");
        assertThat(result.cohortName()).isEqualTo("26-2기");
        assertThat(result.phoneNumber()).isEqualTo("01012345678");
        assertThat(result.hasProfilePhoto()).isFalse();
    }

    @Test
    void 프로필_사진을_교체하면_새_사진을_연결하고_이전_사진을_퇴역시킨다() {
        Member member = member(MEMBER_ID, TEAM_ID, ClubRole.MEMBER, 39L);
        StoredFile newPhoto = profileFile(PROFILE_FILE_ID);
        StoredFile oldPhoto = profileFile(39L);
        given(memberMapper.lookupByIdForUpdate(MEMBER_ID)).willReturn(Optional.of(member));
        given(fileService.uploadProfileImage(org.mockito.ArgumentMatchers.any()))
                .willReturn(PROFILE_FILE_ID);
        given(fileService.lookupProfileImageReadyOwnedBy(PROFILE_FILE_ID, MEMBER_ID))
                .willReturn(newPhoto);
        given(fileService.lookupProfileImageReadyOwnedBy(39L, MEMBER_ID)).willReturn(oldPhoto);
        given(teamMapper.lookupById(TEAM_ID)).willReturn(Optional.of(team(TEAM_ID, "무대팀")));
        given(cohortMapper.lookupById(COHORT_ID)).willReturn(Optional.of(cohort()));

        MemberProfileResponse result = profileService.uploadProfilePhoto(MEMBER_ID,
                new kr.ac.tukorea.bandi.domain.file.service.FileUploadParam("member-profile",
                        "me.png", 4, () -> new java.io.ByteArrayInputStream(new byte[]{1, 2, 3, 4}),
                        MEMBER_ID));

        assertThat(result.hasProfilePhoto()).isTrue();
        verify(memberMapper).updateProfilePhoto(MEMBER_ID, PROFILE_FILE_ID);
        verify(retirementService).queue(oldPhoto);
    }

    @Test
    void 프로필_사진을_삭제하면_연결을_해제하고_퇴역을_등록한다() {
        Member member = member(MEMBER_ID, TEAM_ID, ClubRole.MEMBER, PROFILE_FILE_ID);
        StoredFile photo = profileFile(PROFILE_FILE_ID);
        given(memberMapper.lookupByIdForUpdate(MEMBER_ID)).willReturn(Optional.of(member));
        given(fileService.lookupProfileImageReadyOwnedBy(PROFILE_FILE_ID, MEMBER_ID))
                .willReturn(photo);

        profileService.deleteProfilePhoto(MEMBER_ID);

        verify(memberMapper).updateProfilePhoto(MEMBER_ID, null);
        verify(retirementService).queue(photo);
    }

    @Test
    void 팀장은_현재_팀_멤버만_조회한다() {
        Member leader = member(MEMBER_ID, TEAM_ID, ClubRole.LEADER, null);
        Member teammate = member(11L, TEAM_ID, ClubRole.MEMBER, null);
        given(memberMapper.lookupById(MEMBER_ID)).willReturn(Optional.of(leader));
        given(memberMapper.searchPage(org.mockito.ArgumentMatchers.any()))
                .willReturn(List.of(teammate));
        given(memberMapper.countByPageCondition(org.mockito.ArgumentMatchers.any()))
                .willReturn(1L);
        given(teamMapper.searchAll()).willReturn(List.of(team(TEAM_ID, "무대팀")));
        given(cohortMapper.searchAll()).willReturn(List.of(cohort()));

        var result = profileService.searchTeamMembers(MEMBER_ID,
                new kr.ac.tukorea.bandi.domain.member.dto.request.MemberPageSearchParam(
                        null, null, null, null, null, null, 0, 20));
        assertThat(result.items()).hasSize(1);

        ArgumentCaptor<kr.ac.tukorea.bandi.domain.member.dto.request.MemberPageSearchCondition>
                captor = ArgumentCaptor.forClass(
                kr.ac.tukorea.bandi.domain.member.dto.request.MemberPageSearchCondition.class);
        verify(memberMapper).searchPage(captor.capture());
        assertThat(captor.getValue().teamId()).isEqualTo(TEAM_ID);
        assertThat(result.items()).extracting(item -> item.phoneNumber())
                .containsExactly("01012345678");
    }

    @Test
    void ADMIN은_팀_멤버_목록을_조회할_수_없다() {
        given(memberMapper.lookupById(MEMBER_ID)).willReturn(Optional.of(
                member(MEMBER_ID, TEAM_ID, ClubRole.ADMIN, null)));

        assertThatThrownBy(() -> profileService.searchTeamMembers(MEMBER_ID,
                new kr.ac.tukorea.bandi.domain.member.dto.request.MemberPageSearchParam(
                        null, null, null, null, null, null, 0, 20)))
                .isInstanceOf(MemberManagementForbiddenException.class);
    }

    @Test
    void 일반_멤버는_팀_멤버_목록을_조회할_수_없다() {
        given(memberMapper.lookupById(MEMBER_ID)).willReturn(Optional.of(
                member(MEMBER_ID, TEAM_ID, ClubRole.MEMBER, null)));

        assertThatThrownBy(() -> profileService.searchTeamMembers(MEMBER_ID,
                new kr.ac.tukorea.bandi.domain.member.dto.request.MemberPageSearchParam(
                        null, null, null, null, null, null, 0, 20)))
                .isInstanceOf(MemberManagementForbiddenException.class);
    }

    private Member member(Long memberId, Long teamId, ClubRole role, Long photoFileId) {
        return new Member(memberId, "2025591010", "김현민", "컴퓨터공학부", "01012345678",
                AcademicStatus.ENROLLED, null, teamId, COHORT_ID, role,
                MemberStatus.ACTIVE, SsoLinkStatus.LINKED, null, null, null, photoFileId);
    }

    private Team team(Long teamId, String name) {
        return new Team(teamId, name, 1, true);
    }

    private Cohort cohort() {
        return new Cohort(COHORT_ID, "26-2기", true);
    }

    private StoredFile profileFile(Long storedFileId) {
        StoredFile file = StoredFile.pendingProfileImage("me.png", "member-profile/2026/07/key",
                "image/png", 4, "a".repeat(64), MEMBER_ID);
        file.markReady("etag");
        try {
            var field = StoredFile.class.getDeclaredField("storedFileId");
            field.setAccessible(true);
            field.set(file, storedFileId);
            return file;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
