package kr.ac.tukorea.bandi.domain.performance.service;

import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileConsentParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileCreateParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileVisibilityParam;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProfileViewResponse;
import kr.ac.tukorea.bandi.domain.performance.exception.DuplicatePublicProfileException;
import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPublicProfileConsentException;
import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPublicProfileException;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceAccessDeniedException;
import kr.ac.tukorea.bandi.domain.performance.mapper.PublicProfileMapper;
import kr.ac.tukorea.bandi.domain.performance.model.ConsentScope;
import kr.ac.tukorea.bandi.domain.performance.model.PublicProfile;
import kr.ac.tukorea.bandi.domain.performance.model.PublicProfileConsent;
import kr.ac.tukorea.bandi.domain.performance.model.PublicProfileVisibility;
import kr.ac.tukorea.bandi.domain.policy.service.PolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PublicProfileServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long MEMBER_ID = 2L;
    private static final Long PROFILE_ID = 10L;
    private static final Long VERSION_ID = 20L;
    private static final Long FILE_ID = 30L;
    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 7, 18, 22, 30);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-18T13:30:00Z"),
            ZoneId.of("Asia/Seoul"));

    @Mock
    private PublicProfileMapper publicProfileMapper;
    @Mock
    private MemberService memberService;
    @Mock
    private FileService fileService;
    @Mock
    private PolicyService policyService;

    private PublicProfileService service;

    @BeforeEach
    void setUp() {
        service = new PublicProfileService(publicProfileMapper,
                memberService, fileService, policyService, CLOCK);
    }

    @Test
    void 운영진은_외부_참여자_프로필을_생성하고_공개_파일을_검증한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        willAnswer(invocation -> {
            assignId(invocation.getArgument(0), "publicProfileId", PROFILE_ID);
            return 1;
        }).given(publicProfileMapper).insertProfile(any());

        Long result = service.create(ACTOR_ID, createParam(null));

        assertThat(result).isEqualTo(PROFILE_ID);
        verify(fileService).validatePublicImageReady(FILE_ID);
    }

    @Test
    void 내부_멤버를_연결하면_존재를_검증하고_멤버당_하나만_허용한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(memberService.lookupAccessContext(MEMBER_ID))
                .willReturn(memberContext());
        org.mockito.Mockito.doThrow(new DuplicateKeyException("duplicate"))
                .when(publicProfileMapper).insertProfile(any());

        assertThatThrownBy(() -> service.create(
                ACTOR_ID, createParam(MEMBER_ID)))
                .isInstanceOf(DuplicatePublicProfileException.class);

        verify(memberService).lookupAccessContext(MEMBER_ID);
    }

    @Test
    void 운영진이_아니면_공개_프로필을_관리할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(memberContext());

        assertThatThrownBy(() -> service.create(
                ACTOR_ID, createParam(null)))
                .isInstanceOf(PerformanceAccessDeniedException.class);

        verify(publicProfileMapper, never()).insertProfile(any());
    }

    @Test
    void 발효된_정책_버전으로_항목별_동의를_기록한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(publicProfileMapper.lookupProfileByIdForUpdate(PROFILE_ID))
                .willReturn(Optional.of(profile(PublicProfileVisibility.DRAFT)));
        given(publicProfileMapper.lookupConsentForUpdate(
                PROFILE_ID, VERSION_ID, ConsentScope.PHOTO))
                .willReturn(Optional.empty());

        service.agree(ACTOR_ID, new PublicProfileConsentParam(
                PROFILE_ID, VERSION_ID, ConsentScope.PHOTO));

        verify(policyService).validateEffectiveVersion(VERSION_ID);
        ArgumentCaptor<PublicProfileConsent> captor =
                ArgumentCaptor.forClass(PublicProfileConsent.class);
        verify(publicProfileMapper).insertConsent(captor.capture());
        assertThat(captor.getValue().getAgreedDttm()).isEqualTo(NOW);
    }

    @Test
    void 사진이_없는_프로필은_사진_공개에_동의할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(publicProfileMapper.lookupProfileByIdForUpdate(PROFILE_ID))
                .willReturn(Optional.of(new PublicProfile(
                        PROFILE_ID, null, "배우", "소개", null,
                        null, PublicProfileVisibility.DRAFT, null, null)));

        assertThatThrownBy(() -> service.agree(ACTOR_ID,
                new PublicProfileConsentParam(
                        PROFILE_ID, VERSION_ID, ConsentScope.PHOTO)))
                .isInstanceOf(InvalidPublicProfileException.class);
    }

    @Test
    void 동시에_같은_동의를_기록하면_중복_오류로_변환한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(publicProfileMapper.lookupProfileByIdForUpdate(PROFILE_ID))
                .willReturn(Optional.of(profile(PublicProfileVisibility.DRAFT)));
        given(publicProfileMapper.lookupConsentForUpdate(
                PROFILE_ID, VERSION_ID, ConsentScope.NAME))
                .willReturn(Optional.empty());
        org.mockito.Mockito.doThrow(new DuplicateKeyException("duplicate"))
                .when(publicProfileMapper).insertConsent(any());

        assertThatThrownBy(() -> service.agree(ACTOR_ID,
                new PublicProfileConsentParam(
                        PROFILE_ID, VERSION_ID, ConsentScope.NAME)))
                .isInstanceOf(InvalidPublicProfileConsentException.class);
    }

    @Test
    void 동의를_철회하면_같은_행에_철회_시각을_기록한다() {
        PublicProfileConsent consent = new PublicProfileConsent(
                40L, PROFILE_ID, VERSION_ID, ConsentScope.NAME,
                true, NOW.minusDays(1), null, ACTOR_ID);
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(publicProfileMapper.lookupConsentByIdForUpdate(40L))
                .willReturn(Optional.of(consent));

        service.revoke(ACTOR_ID, 40L);

        ArgumentCaptor<PublicProfileConsent> captor =
                ArgumentCaptor.forClass(PublicProfileConsent.class);
        verify(publicProfileMapper).updateConsent(captor.capture());
        assertThat(captor.getValue().getRevokedDttm()).isEqualTo(NOW);
    }

    @Test
    void 외부_조회는_게시_프로필의_동의한_항목만_노출한다() {
        PublicProfile published = profile(PublicProfileVisibility.PUBLISHED);
        given(publicProfileMapper.lookupPublishedById(PROFILE_ID))
                .willReturn(Optional.of(published));
        given(publicProfileMapper.searchLatestAgreedScopes(PROFILE_ID, NOW))
                .willReturn(EnumSet.of(ConsentScope.NAME, ConsentScope.BIO));

        PublicProfileViewResponse result = service.lookupPublic(PROFILE_ID);

        assertThat(result.publicName()).isEqualTo("배우");
        assertThat(result.bio()).isEqualTo("소개");
        assertThat(result.profileFileId()).isNull();
        assertThat(result.socialUrl()).isNull();
    }

    @Test
    void 이름_동의가_없으면_외부_공개_후보가_아니다() {
        given(publicProfileMapper.lookupPublishedById(PROFILE_ID))
                .willReturn(Optional.of(profile(PublicProfileVisibility.PUBLISHED)));
        given(publicProfileMapper.searchLatestAgreedScopes(PROFILE_ID, NOW))
                .willReturn(EnumSet.of(ConsentScope.BIO));

        assertThat(service.lookupPublicCandidate(PROFILE_ID)).isEmpty();
    }

    @Test
    void 게시와_보관은_운영진만_상태를_변경한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(publicProfileMapper.lookupProfileByIdForUpdate(PROFILE_ID))
                .willReturn(Optional.of(profile(PublicProfileVisibility.DRAFT)));

        service.changeVisibility(ACTOR_ID,
                new PublicProfileVisibilityParam(
                        PROFILE_ID, PublicProfileVisibility.PUBLISHED));

        verify(publicProfileMapper).updateProfile(any());
    }

    private PublicProfileCreateParam createParam(Long memberId) {
        return new PublicProfileCreateParam(memberId, "배우", "소개",
                FILE_ID, "https://example.com/actor");
    }

    private PublicProfile profile(PublicProfileVisibility visibility) {
        return new PublicProfile(PROFILE_ID, MEMBER_ID, "배우", "소개",
                FILE_ID, "https://example.com/actor", visibility,
                null, null);
    }

    private MemberAccessContext adminContext() {
        return new MemberAccessContext(ACTOR_ID, 1L,
                true, false, true);
    }

    private MemberAccessContext memberContext() {
        return new MemberAccessContext(MEMBER_ID, 1L,
                false, false, true);
    }

    private void assignId(Object target, String fieldName, Long value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
