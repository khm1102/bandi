package kr.ac.tukorea.bandi.domain.performance.service;

import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundAccessibilityWriteParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundStatusParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundWriteParam;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformancePublicPageResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceRoundAccessibilityResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceRoundResponse;
import kr.ac.tukorea.bandi.domain.performance.exception.DuplicatePerformanceContentException;
import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceAccessDeniedException;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformanceRoundMapper;
import kr.ac.tukorea.bandi.domain.performance.model.AccessibilitySupportType;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRound;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRoundAccessibility;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRoundStatus;
import kr.ac.tukorea.bandi.domain.performance.model.PublicPageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
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
class PerformanceRoundServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long PROJECT_ID = 10L;
    private static final Long ROUND_ID = 20L;
    private static final Long ACCESSIBILITY_ID = 30L;
    private static final LocalDateTime RESERVATION_OPEN =
            LocalDateTime.of(2026, 11, 1, 10, 0);
    private static final LocalDateTime RESERVATION_CLOSE =
            LocalDateTime.of(2026, 11, 20, 18, 0);
    private static final LocalDateTime ENTRY_START =
            LocalDateTime.of(2026, 11, 21, 18, 30);
    private static final LocalDateTime START =
            LocalDateTime.of(2026, 11, 21, 19, 0);

    @Mock
    private PerformanceRoundMapper roundMapper;
    @Mock
    private MemberService memberService;
    @Mock
    private PerformanceProjectService projectService;
    @Mock
    private PerformancePublicPageService publicPageService;

    private PerformanceRoundService service;

    @BeforeEach
    void setUp() {
        service = new PerformanceRoundService(roundMapper, memberService,
                projectService, publicPageService);
    }

    @Test
    void 운영진은_공연_회차를_생성한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        willAnswer(invocation -> {
            assignId(invocation.getArgument(0),
                    "performanceRoundId", ROUND_ID);
            return 1;
        }).given(roundMapper).insertRound(any());

        Long result = service.createRound(ACTOR_ID, roundParam(null));

        assertThat(result).isEqualTo(ROUND_ID);
        verify(projectService).validateExists(ACTOR_ID, PROJECT_ID);
    }

    @Test
    void 운영진이_아니면_회차를_관리할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(memberContext());

        assertThatThrownBy(() -> service.createRound(
                ACTOR_ID, roundParam(null)))
                .isInstanceOf(PerformanceAccessDeniedException.class);

        verify(roundMapper, never()).insertRound(any());
    }

    @Test
    void 같은_프로젝트의_회차_번호는_중복될_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(roundMapper.insertRound(any()))
                .willThrow(DuplicateKeyException.class);

        assertThatThrownBy(() -> service.createRound(
                ACTOR_ID, roundParam(null)))
                .isInstanceOf(DuplicatePerformanceContentException.class);
    }

    @Test
    void 다른_프로젝트의_회차로_수정할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(roundMapper.lookupRoundForUpdate(ROUND_ID))
                .willReturn(Optional.of(round(
                        PerformanceRoundStatus.SCHEDULED)));
        PerformanceRoundWriteParam param = new PerformanceRoundWriteParam(
                ROUND_ID, 99L, 1, START, ENTRY_START,
                RESERVATION_OPEN, RESERVATION_CLOSE);

        assertThatThrownBy(() -> service.updateRound(ACTOR_ID, param))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }

    @Test
    void 회차_상태를_변경한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(roundMapper.lookupRoundForUpdate(ROUND_ID))
                .willReturn(Optional.of(round(
                        PerformanceRoundStatus.SCHEDULED)));

        service.changeRoundStatus(ACTOR_ID,
                new PerformanceRoundStatusParam(ROUND_ID,
                        PerformanceRoundStatus.RESERVATION_OPEN));

        verify(roundMapper).updateRound(any());
    }

    @Test
    void 같은_회차에_같은_접근성_유형은_한_번만_등록한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(roundMapper.lookupRoundForUpdate(ROUND_ID))
                .willReturn(Optional.of(round(
                        PerformanceRoundStatus.SCHEDULED)));
        given(roundMapper.insertAccessibility(any()))
                .willThrow(DuplicateKeyException.class);

        assertThatThrownBy(() -> service.createAccessibility(
                ACTOR_ID, accessibilityParam(null)))
                .isInstanceOf(DuplicatePerformanceContentException.class);
    }

    @Test
    void 접근성_지원은_다른_회차로_옮길_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(roundMapper.lookupAccessibilityForUpdate(ACCESSIBILITY_ID))
                .willReturn(Optional.of(accessibility()));
        PerformanceRoundAccessibilityWriteParam param =
                new PerformanceRoundAccessibilityWriteParam(
                        ACCESSIBILITY_ID, 99L,
                        AccessibilitySupportType.CAPTION,
                        "한글 자막", null, 0);

        assertThatThrownBy(() -> service.updateAccessibility(
                ACTOR_ID, param))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }

    @Test
    void 공개_회차에는_취소_상태와_접근성_지원을_함께_표시한다() {
        given(publicPageService.lookupPublic("hamlet-2026"))
                .willReturn(publicPage());
        given(roundMapper.searchRounds(PROJECT_ID)).willReturn(List.of(
                roundResponse(PerformanceRoundStatus.SCHEDULED),
                new PerformanceRoundResponse(21L, PROJECT_ID, 2,
                        START.plusDays(1), ENTRY_START.plusDays(1),
                        RESERVATION_OPEN, RESERVATION_CLOSE,
                        PerformanceRoundStatus.CANCELLED)));
        given(roundMapper.searchAccessibilitiesByProject(PROJECT_ID))
                .willReturn(List.of(accessibilityResponse()));

        var result = service.searchPublicRounds("hamlet-2026");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).accessibilities()).hasSize(1);
        assertThat(result.get(1).status())
                .isEqualTo(PerformanceRoundStatus.CANCELLED);
    }

    @Test
    void 활성_멤버는_회차와_프로젝트_관계를_검증한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(memberContext());
        given(roundMapper.lookupRoundForUpdate(ROUND_ID))
                .willReturn(Optional.of(round(
                        PerformanceRoundStatus.SCHEDULED)));

        service.validateExists(ACTOR_ID, ROUND_ID, PROJECT_ID);

        verify(roundMapper).lookupRoundForUpdate(ROUND_ID);
    }

    private PerformanceRoundWriteParam roundParam(Long id) {
        return new PerformanceRoundWriteParam(id, PROJECT_ID, 1,
                START, ENTRY_START, RESERVATION_OPEN, RESERVATION_CLOSE);
    }

    private PerformanceRoundAccessibilityWriteParam accessibilityParam(
            Long id) {
        return new PerformanceRoundAccessibilityWriteParam(id, ROUND_ID,
                AccessibilitySupportType.CAPTION,
                "한글 자막", "무대 상단", 0);
    }

    private PerformanceRound round(PerformanceRoundStatus status) {
        return new PerformanceRound(ROUND_ID, PROJECT_ID, 1,
                START, ENTRY_START, RESERVATION_OPEN, RESERVATION_CLOSE,
                status, null, null);
    }

    private PerformanceRoundAccessibility accessibility() {
        return new PerformanceRoundAccessibility(ACCESSIBILITY_ID,
                ROUND_ID, AccessibilitySupportType.CAPTION,
                "한글 자막", "무대 상단", 0, null, null);
    }

    private PerformanceRoundResponse roundResponse(
            PerformanceRoundStatus status) {
        return new PerformanceRoundResponse(ROUND_ID, PROJECT_ID, 1,
                START, ENTRY_START, RESERVATION_OPEN, RESERVATION_CLOSE,
                status);
    }

    private PerformanceRoundAccessibilityResponse accessibilityResponse() {
        return new PerformanceRoundAccessibilityResponse(
                ACCESSIBILITY_ID, ROUND_ID,
                AccessibilitySupportType.CAPTION,
                "한글 자막", "무대 상단", 0);
    }

    private PerformancePublicPageResponse publicPage() {
        return new PerformancePublicPageResponse(
                100L, PROJECT_ID, "햄릿", null, null, "소극장",
                "hamlet-2026", PublicPageStatus.PUBLISHED,
                "소개", "시놉시스", null, "비극", "12세", 120,
                null, 0L, null, null, null, "문의", "채널", "Bandi",
                null, null, null, null, null);
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
