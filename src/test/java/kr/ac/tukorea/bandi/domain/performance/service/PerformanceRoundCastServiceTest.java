package kr.ac.tukorea.bandi.domain.performance.service;

import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundCastAssignParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundCastChangeParam;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceRoundCastResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicPerformanceRoundResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProfileViewResponse;
import kr.ac.tukorea.bandi.domain.performance.exception.DuplicatePerformanceContentException;
import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceAccessDeniedException;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformanceContentMapper;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformanceRoundCastMapper;
import kr.ac.tukorea.bandi.domain.performance.model.CastAction;
import kr.ac.tukorea.bandi.domain.performance.model.CastType;
import kr.ac.tukorea.bandi.domain.performance.model.CharacterImportance;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceCastHistory;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceCharacter;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRoundCast;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRoundStatus;
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
class PerformanceRoundCastServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long PROJECT_ID = 10L;
    private static final Long ROUND_ID = 20L;
    private static final Long CHARACTER_ID = 30L;
    private static final Long PROFILE_ID = 40L;
    private static final Long NEW_PROFILE_ID = 41L;
    private static final Long ROUND_CAST_ID = 50L;
    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 11, 21, 17, 0);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-21T08:00:00Z"),
            ZoneId.of("Asia/Seoul"));

    @Mock
    private PerformanceRoundCastMapper roundCastMapper;
    @Mock
    private PerformanceContentMapper contentMapper;
    @Mock
    private MemberService memberService;
    @Mock
    private PerformanceRoundService roundService;
    @Mock
    private PublicProfileService publicProfileService;

    private PerformanceRoundCastService service;

    @BeforeEach
    void setUp() {
        service = new PerformanceRoundCastService(roundCastMapper,
                contentMapper, memberService, roundService,
                publicProfileService, CLOCK);
    }

    @Test
    void 운영진은_회차의_실제_출연자를_배정하고_이력을_남긴다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(contentMapper.lookupCharacterForUpdate(CHARACTER_ID))
                .willReturn(Optional.of(character()));
        given(publicProfileService.lookupPublicCandidate(PROFILE_ID))
                .willReturn(Optional.of(profile(PROFILE_ID)));
        willAnswer(invocation -> {
            assignId(invocation.getArgument(0), ROUND_CAST_ID);
            return 1;
        }).given(roundCastMapper).insert(any());

        Long result = service.assign(ACTOR_ID, assignParam());

        assertThat(result).isEqualTo(ROUND_CAST_ID);
        verify(roundService).validateExists(
                ACTOR_ID, ROUND_ID, PROJECT_ID);
        ArgumentCaptor<PerformanceCastHistory> historyCaptor =
                ArgumentCaptor.forClass(PerformanceCastHistory.class);
        verify(contentMapper).insertCastHistory(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getPerformanceRoundId())
                .isEqualTo(ROUND_ID);
        assertThat(historyCaptor.getValue().getAction())
                .isEqualTo(CastAction.ASSIGN);
    }

    @Test
    void 운영진이_아니면_회차_캐스팅을_관리할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(memberContext());

        assertThatThrownBy(() -> service.assign(ACTOR_ID, assignParam()))
                .isInstanceOf(PerformanceAccessDeniedException.class);

        verify(roundCastMapper, never()).insert(any());
    }

    @Test
    void 다른_프로젝트의_등장인물은_회차에_배정할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(contentMapper.lookupCharacterForUpdate(CHARACTER_ID))
                .willReturn(Optional.of(character()));
        PerformanceRoundCastAssignParam mismatched =
                new PerformanceRoundCastAssignParam(
                        99L, ROUND_ID, CHARACTER_ID, PROFILE_ID,
                        CastType.PRIMARY, null);

        assertThatThrownBy(() -> service.assign(ACTOR_ID, mismatched))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }

    @Test
    void 공개_동의가_없는_프로필은_배정할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(contentMapper.lookupCharacterForUpdate(CHARACTER_ID))
                .willReturn(Optional.of(character()));
        given(publicProfileService.lookupPublicCandidate(PROFILE_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.assign(ACTOR_ID, assignParam()))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }

    @Test
    void 같은_회차의_같은_배역에는_한_명만_배정한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(contentMapper.lookupCharacterForUpdate(CHARACTER_ID))
                .willReturn(Optional.of(character()));
        given(publicProfileService.lookupPublicCandidate(PROFILE_ID))
                .willReturn(Optional.of(profile(PROFILE_ID)));
        given(roundCastMapper.insert(any()))
                .willThrow(DuplicateKeyException.class);

        assertThatThrownBy(() -> service.assign(ACTOR_ID, assignParam()))
                .isInstanceOf(DuplicatePerformanceContentException.class);
    }

    @Test
    void 회차_캐스팅을_교체하고_제거하면_각각_이력을_남긴다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(roundCastMapper.lookupByIdForUpdate(ROUND_CAST_ID))
                .willReturn(Optional.of(cast()), Optional.of(cast()));
        given(publicProfileService.lookupPublicCandidate(NEW_PROFILE_ID))
                .willReturn(Optional.of(profile(NEW_PROFILE_ID)));

        service.change(ACTOR_ID, new PerformanceRoundCastChangeParam(
                ROUND_CAST_ID, NEW_PROFILE_ID,
                CastType.ALTERNATE, "출연 일정 변경"));
        service.remove(ACTOR_ID, ROUND_CAST_ID, "회차 출연 취소");

        verify(roundCastMapper).update(any());
        verify(roundCastMapper).remove(ROUND_CAST_ID);
        ArgumentCaptor<PerformanceCastHistory> historyCaptor =
                ArgumentCaptor.forClass(PerformanceCastHistory.class);
        verify(contentMapper, org.mockito.Mockito.times(2))
                .insertCastHistory(historyCaptor.capture());
        assertThat(historyCaptor.getAllValues())
                .extracting(PerformanceCastHistory::getAction)
                .containsExactly(CastAction.CHANGE, CastAction.REMOVE);
        assertThat(historyCaptor.getAllValues())
                .extracting(PerformanceCastHistory::getChangedDttm)
                .containsOnly(NOW);
    }

    @Test
    void 공개_조회는_현재_공개_동의가_유효한_출연자만_반환한다() {
        given(roundService.searchPublicRounds("hamlet-2026"))
                .willReturn(List.of(publicRound()));
        given(roundCastMapper.searchByRound(ROUND_ID)).willReturn(List.of(
                response(PROFILE_ID), response(NEW_PROFILE_ID)));
        given(publicProfileService.lookupPublicCandidate(PROFILE_ID))
                .willReturn(Optional.of(profile(PROFILE_ID)));
        given(publicProfileService.lookupPublicCandidate(NEW_PROFILE_ID))
                .willReturn(Optional.empty());

        assertThat(service.searchPublic("hamlet-2026", ROUND_ID))
                .hasSize(1).first()
                .extracting("profile.publicProfileId")
                .isEqualTo(PROFILE_ID);
    }

    private PerformanceRoundCastAssignParam assignParam() {
        return new PerformanceRoundCastAssignParam(
                PROJECT_ID, ROUND_ID, CHARACTER_ID, PROFILE_ID,
                CastType.PRIMARY, "최초 배정");
    }

    private PerformanceCharacter character() {
        return new PerformanceCharacter(CHARACTER_ID, PROJECT_ID,
                "햄릿", "덴마크의 왕자", CharacterImportance.LEAD, 0);
    }

    private PerformanceRoundCast cast() {
        return new PerformanceRoundCast(ROUND_CAST_ID, PROJECT_ID,
                ROUND_ID, CHARACTER_ID, PROFILE_ID,
                CastType.PRIMARY, null, null);
    }

    private PerformanceRoundCastResponse response(Long profileId) {
        return new PerformanceRoundCastResponse(ROUND_CAST_ID,
                PROJECT_ID, ROUND_ID, CHARACTER_ID,
                "햄릿", "덴마크의 왕자", CharacterImportance.LEAD,
                profileId, CastType.PRIMARY);
    }

    private PublicProfileViewResponse profile(Long profileId) {
        return new PublicProfileViewResponse(
                profileId, "배우", null, null, null);
    }

    private PublicPerformanceRoundResponse publicRound() {
        return new PublicPerformanceRoundResponse(ROUND_ID, 1,
                LocalDateTime.of(2026, 11, 21, 19, 0),
                LocalDateTime.of(2026, 11, 21, 18, 30),
                LocalDateTime.of(2026, 11, 1, 10, 0),
                LocalDateTime.of(2026, 11, 20, 18, 0),
                PerformanceRoundStatus.SCHEDULED, List.of());
    }

    private MemberAccessContext adminContext() {
        return new MemberAccessContext(ACTOR_ID, 1L,
                true, false, true);
    }

    private MemberAccessContext memberContext() {
        return new MemberAccessContext(ACTOR_ID, 1L,
                false, false, true);
    }

    private void assignId(Object target, Long value) {
        try {
            Field field = target.getClass()
                    .getDeclaredField("performanceRoundCastId");
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
