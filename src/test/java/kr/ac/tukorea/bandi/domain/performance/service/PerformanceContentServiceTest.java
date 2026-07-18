package kr.ac.tukorea.bandi.domain.performance.service;

import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceCastAssignParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceCastChangeParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceCharacterWriteParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceMediaWriteParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.ProductionCreditWriteParam;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceCastResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformancePublicPageResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.ProductionCreditResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProfileViewResponse;
import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceAccessDeniedException;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformanceContentMapper;
import kr.ac.tukorea.bandi.domain.performance.model.CastAction;
import kr.ac.tukorea.bandi.domain.performance.model.CastType;
import kr.ac.tukorea.bandi.domain.performance.model.CharacterImportance;
import kr.ac.tukorea.bandi.domain.performance.model.MediaType;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceCast;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceCastHistory;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceCharacter;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceMedia;
import kr.ac.tukorea.bandi.domain.performance.model.ProductionCredit;
import kr.ac.tukorea.bandi.domain.performance.model.PublicPageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PerformanceContentServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long PROJECT_ID = 10L;
    private static final Long CHARACTER_ID = 20L;
    private static final Long CAST_ID = 30L;
    private static final Long PROFILE_ID = 40L;
    private static final Long NEW_PROFILE_ID = 41L;
    private static final Long FILE_ID = 50L;
    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 8, 2, 12, 0);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-08-02T03:00:00Z"),
            ZoneId.of("Asia/Seoul"));

    @Mock
    private PerformanceContentMapper contentMapper;
    @Mock
    private MemberService memberService;
    @Mock
    private PerformanceProjectService projectService;
    @Mock
    private PerformancePublicPageService publicPageService;
    @Mock
    private PublicProfileService publicProfileService;
    @Mock
    private FileService fileService;

    private PerformanceContentService service;

    @BeforeEach
    void setUp() {
        service = new PerformanceContentService(contentMapper,
                memberService, projectService, publicPageService,
                publicProfileService, fileService, CLOCK);
    }

    @Test
    void 운영진은_등장인물을_생성한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        willAnswer(invocation -> {
            assignId(invocation.getArgument(0),
                    "performanceCharacterId", CHARACTER_ID);
            return 1;
        }).given(contentMapper).insertCharacter(any());

        Long result = service.createCharacter(
                ACTOR_ID, characterParam(null));

        assertThat(result).isEqualTo(CHARACTER_ID);
        verify(projectService).validateExists(ACTOR_ID, PROJECT_ID);
    }

    @Test
    void 운영진이_아니면_공연_콘텐츠를_관리할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(memberContext());

        assertThatThrownBy(() -> service.createCharacter(
                ACTOR_ID, characterParam(null)))
                .isInstanceOf(PerformanceAccessDeniedException.class);

        verify(contentMapper, never()).insertCharacter(any());
    }

    @Test
    void 캐스팅이_남은_등장인물은_삭제할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(contentMapper.lookupCharacterForUpdate(CHARACTER_ID))
                .willReturn(Optional.of(character()));
        given(contentMapper.existsCastByCharacter(CHARACTER_ID))
                .willReturn(true);

        assertThatThrownBy(() -> service.removeCharacter(
                ACTOR_ID, CHARACTER_ID))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }

    @Test
    void 캐스팅_변경_이력이_남은_등장인물도_삭제할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(contentMapper.lookupCharacterForUpdate(CHARACTER_ID))
                .willReturn(Optional.of(character()));
        given(contentMapper.existsCastByCharacter(CHARACTER_ID))
                .willReturn(false);
        given(contentMapper.existsCastHistoryByCharacter(CHARACTER_ID))
                .willReturn(true);

        assertThatThrownBy(() -> service.removeCharacter(
                ACTOR_ID, CHARACTER_ID))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }

    @Test
    void 공개_동의가_유효한_프로필을_캐스팅하고_이력을_남긴다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(contentMapper.lookupCharacterForUpdate(CHARACTER_ID))
                .willReturn(Optional.of(character()));
        given(publicProfileService.lookupPublicCandidate(PROFILE_ID))
                .willReturn(Optional.of(nullProfileView(PROFILE_ID)));

        service.assignCast(ACTOR_ID, new PerformanceCastAssignParam(
                PROJECT_ID, CHARACTER_ID, PROFILE_ID,
                CastType.PRIMARY, 0, "최초 배정"));

        verify(contentMapper).insertCast(any());
        ArgumentCaptor<PerformanceCastHistory> historyCaptor =
                ArgumentCaptor.forClass(PerformanceCastHistory.class);
        verify(contentMapper).insertCastHistory(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getAction())
                .isEqualTo(CastAction.ASSIGN);
        assertThat(historyCaptor.getValue().getChangedDttm()).isEqualTo(NOW);
    }

    @Test
    void 공개_동의가_없는_프로필은_캐스팅할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(contentMapper.lookupCharacterForUpdate(CHARACTER_ID))
                .willReturn(Optional.of(character()));
        given(publicProfileService.lookupPublicCandidate(PROFILE_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignCast(ACTOR_ID,
                new PerformanceCastAssignParam(PROJECT_ID, CHARACTER_ID,
                        PROFILE_ID, CastType.PRIMARY, 0, null)))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }

    @Test
    void 캐스팅을_변경하고_제거할_때_각각_이력을_남긴다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(contentMapper.lookupCastForUpdate(CAST_ID))
                .willReturn(Optional.of(cast()), Optional.of(cast()));
        given(publicProfileService.lookupPublicCandidate(NEW_PROFILE_ID))
                .willReturn(Optional.of(nullProfileView(NEW_PROFILE_ID)));

        service.changeCast(ACTOR_ID, new PerformanceCastChangeParam(
                CAST_ID, NEW_PROFILE_ID, CastType.ALTERNATE,
                1, "출연 일정 변경"));
        service.removeCast(ACTOR_ID, CAST_ID, "배역 정리");

        verify(contentMapper).updateCast(any());
        verify(contentMapper).removeCast(CAST_ID);
        ArgumentCaptor<PerformanceCastHistory> historyCaptor =
                ArgumentCaptor.forClass(PerformanceCastHistory.class);
        verify(contentMapper,
                org.mockito.Mockito.times(2))
                .insertCastHistory(historyCaptor.capture());
        assertThat(historyCaptor.getAllValues())
                .extracting(PerformanceCastHistory::getAction)
                .containsExactly(CastAction.CHANGE, CastAction.REMOVE);
    }

    @Test
    void 제작진_프로필을_연결할_때_공개_동의를_검증한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(publicProfileService.lookupPublicCandidate(PROFILE_ID))
                .willReturn(Optional.of(nullProfileView(PROFILE_ID)));

        service.createCredit(ACTOR_ID,
                new ProductionCreditWriteParam(null, PROJECT_ID,
                        "연출", "김연출", PROFILE_ID, 0));

        verify(contentMapper).insertCredit(any());
    }

    @Test
    void 공연_미디어는_PUBLIC_READY_파일만_연결한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());

        service.createMedia(ACTOR_ID, mediaParam(null));

        verify(fileService).validatePublicImageReady(FILE_ID);
        verify(contentMapper).insertMedia(any());
    }

    @Test
    void 공연_미디어_유형을_변경하면_파일_유형을_다시_검증한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(contentMapper.lookupMediaForUpdate(60L))
                .willReturn(Optional.of(new PerformanceMedia(
                        60L, PROJECT_ID, FILE_ID, MediaType.VIDEO,
                        "연습 영상", "1막 연습", "1막 연습 영상",
                        "촬영 영상팀", "https://example.com/video",
                        0, false)));

        service.updateMedia(ACTOR_ID, new PerformanceMediaWriteParam(
                60L, PROJECT_ID, FILE_ID, MediaType.POSTER,
                "공연 포스터", "공식 공연 포스터", "공연 포스터",
                "디자인팀", null, 0));

        verify(fileService).validatePublicImageReady(FILE_ID);
        verify(contentMapper).updateMedia(any());
    }

    @Test
    void 공개_크레딧은_연결_프로필의_현재_동의가_철회되면_제외한다() {
        given(publicPageService.lookupPublic("hamlet-2026"))
                .willReturn(publicPage());
        given(contentMapper.searchCredits(PROJECT_ID)).willReturn(List.of(
                creditResponse(1L, null),
                creditResponse(2L, PROFILE_ID)));
        given(publicProfileService.lookupPublicCandidate(PROFILE_ID))
                .willReturn(Optional.empty());

        assertThat(service.searchPublicCredits("hamlet-2026"))
                .extracting("productionCreditId")
                .containsExactly(1L);
    }

    @Test
    void 공개_캐스팅은_현재_공개_가능한_프로필만_반환한다() {
        given(publicPageService.lookupPublic("hamlet-2026"))
                .willReturn(publicPage());
        given(contentMapper.searchCasts(PROJECT_ID)).willReturn(List.of(
                castResponse(PROFILE_ID), castResponse(NEW_PROFILE_ID)));
        given(publicProfileService.lookupPublicCandidate(PROFILE_ID))
                .willReturn(Optional.of(nullProfileView(PROFILE_ID)));
        given(publicProfileService.lookupPublicCandidate(NEW_PROFILE_ID))
                .willReturn(Optional.empty());

        assertThat(service.searchPublicCasts("hamlet-2026"))
                .hasSize(1);
    }

    private PerformanceCharacterWriteParam characterParam(Long id) {
        return new PerformanceCharacterWriteParam(id, PROJECT_ID,
                "햄릿", "덴마크의 왕자", CharacterImportance.LEAD, 0);
    }

    private PerformanceCharacter character() {
        return new PerformanceCharacter(CHARACTER_ID, PROJECT_ID,
                "햄릿", "덴마크의 왕자", CharacterImportance.LEAD, 0);
    }

    private PerformanceCast cast() {
        return new PerformanceCast(CAST_ID, PROJECT_ID, CHARACTER_ID,
                PROFILE_ID, CastType.PRIMARY, 0);
    }

    private PerformanceMediaWriteParam mediaParam(Long id) {
        return new PerformanceMediaWriteParam(id, PROJECT_ID, FILE_ID,
                MediaType.REHEARSAL, "연습실 사진", "1막 연습",
                "배우들이 연습하는 모습", "촬영 영상팀", null, 0);
    }

    private PublicProfileViewResponse nullProfileView(Long profileId) {
        return new PublicProfileViewResponse(
                profileId, "배우", null, null, null);
    }

    private PerformancePublicPageResponse publicPage() {
        return new PerformancePublicPageResponse(
                100L, PROJECT_ID, "햄릿", null, null, "소극장",
                "hamlet-2026", PublicPageStatus.PUBLISHED,
                "소개", "시놉시스", null, "비극", "12세", 120,
                null, 0L, null, null, null, "문의", "채널", "Bandi",
                null, null, null, null, null);
    }

    private PerformanceCastResponse castResponse(Long profileId) {
        return new PerformanceCastResponse(CAST_ID, PROJECT_ID,
                CHARACTER_ID, "햄릿", "덴마크의 왕자",
                CharacterImportance.LEAD, profileId, CastType.PRIMARY, 0);
    }

    private ProductionCreditResponse creditResponse(Long id,
                                                     Long profileId) {
        return new ProductionCreditResponse(id, PROJECT_ID,
                "연출", "김연출", profileId, 0);
    }

    private MemberAccessContext adminContext() {
        return new MemberAccessContext(ACTOR_ID, 1L,
                true, false, true);
    }

    private MemberAccessContext memberContext() {
        return new MemberAccessContext(ACTOR_ID, 1L,
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
