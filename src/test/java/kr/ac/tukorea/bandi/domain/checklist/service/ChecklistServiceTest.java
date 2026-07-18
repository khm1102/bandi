package kr.ac.tukorea.bandi.domain.checklist.service;

import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistCompletionParam;
import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistItemCreateParam;
import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistItemSearchCondition;
import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistItemUpdateParam;
import kr.ac.tukorea.bandi.domain.checklist.exception.ChecklistAccessDeniedException;
import kr.ac.tukorea.bandi.domain.checklist.mapper.ChecklistMapper;
import kr.ac.tukorea.bandi.domain.checklist.model.ChecklistItem;
import kr.ac.tukorea.bandi.domain.checklist.model.ChecklistItemHistory;
import kr.ac.tukorea.bandi.domain.checklist.model.ChecklistScope;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceProjectService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundService;
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
class ChecklistServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long PROJECT_ID = 10L;
    private static final Long ROUND_ID = 20L;
    private static final Long TEAM_ID = 30L;
    private static final Long OTHER_TEAM_ID = 31L;
    private static final Long ITEM_ID = 40L;
    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 11, 21, 17, 0);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-21T08:00:00Z"),
            ZoneId.of("Asia/Seoul"));

    @Mock
    private ChecklistMapper checklistMapper;
    @Mock
    private MemberService memberService;
    @Mock
    private PerformanceProjectService projectService;
    @Mock
    private PerformanceRoundService roundService;

    private ChecklistService service;

    @BeforeEach
    void setUp() {
        service = new ChecklistService(checklistMapper, memberService,
                projectService, roundService, CLOCK);
    }

    @Test
    void 팀장은_소속_팀의_프로젝트_체크리스트를_생성한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(leaderContext());
        willAnswer(invocation -> {
            assignId(invocation.getArgument(0), ITEM_ID);
            return 1;
        }).given(checklistMapper).insert(any());

        Long result = service.create(ACTOR_ID, projectParam());

        assertThat(result).isEqualTo(ITEM_ID);
        verify(memberService).validateActiveTeam(TEAM_ID);
        verify(projectService).validateProductionMutable(
                ACTOR_ID, PROJECT_ID);
    }

    @Test
    void 일반_멤버는_체크리스트_항목을_생성할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(memberContext(TEAM_ID));

        assertThatThrownBy(() -> service.create(ACTOR_ID, projectParam()))
                .isInstanceOf(ChecklistAccessDeniedException.class);

        verify(checklistMapper, never()).insert(any());
    }

    @Test
    void 팀장은_다른_팀의_항목을_관리할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(leaderContext());
        ChecklistItemCreateParam otherTeam =
                new ChecklistItemCreateParam(PROJECT_ID, null,
                        OTHER_TEAM_ID, ChecklistScope.PROJECT,
                        "조명 확인", true, 0);

        assertThatThrownBy(() -> service.create(ACTOR_ID, otherTeam))
                .isInstanceOf(ChecklistAccessDeniedException.class);
    }

    @Test
    void 회차별_항목은_회차와_프로젝트_관계를_확인한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());

        service.create(ACTOR_ID, roundParam());

        verify(roundService).validateExists(
                ACTOR_ID, ROUND_ID, PROJECT_ID);
    }

    @Test
    void 같은_팀_멤버는_항목을_완료하고_이력을_남긴다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(memberContext(TEAM_ID));
        given(checklistMapper.lookupByIdForUpdate(ITEM_ID))
                .willReturn(Optional.of(item(false)));

        service.changeCompleted(ACTOR_ID,
                new ChecklistCompletionParam(
                        ITEM_ID, true, "현장 확인"));

        ArgumentCaptor<ChecklistItem> itemCaptor =
                ArgumentCaptor.forClass(ChecklistItem.class);
        verify(checklistMapper).update(itemCaptor.capture());
        assertThat(itemCaptor.getValue().isCompleted()).isTrue();
        assertThat(itemCaptor.getValue().getCompletedDttm()).isEqualTo(NOW);
        ArgumentCaptor<ChecklistItemHistory> historyCaptor =
                ArgumentCaptor.forClass(ChecklistItemHistory.class);
        verify(checklistMapper).insertHistory(historyCaptor.capture());
        assertThat(historyCaptor.getValue().isPreviousCompleted()).isFalse();
        assertThat(historyCaptor.getValue().isNewCompleted()).isTrue();
    }

    @Test
    void 다른_팀_멤버는_완료_상태를_바꿀_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(memberContext(OTHER_TEAM_ID));
        given(checklistMapper.lookupByIdForUpdate(ITEM_ID))
                .willReturn(Optional.of(item(false)));

        assertThatThrownBy(() -> service.changeCompleted(ACTOR_ID,
                new ChecklistCompletionParam(ITEM_ID, true, null)))
                .isInstanceOf(ChecklistAccessDeniedException.class);

        verify(checklistMapper, never()).update(any());
    }

    @Test
    void 팀장은_소속_팀_항목을_수정하고_삭제한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(leaderContext());
        given(checklistMapper.lookupByIdForUpdate(ITEM_ID))
                .willReturn(Optional.of(item(false)),
                        Optional.of(item(false)));

        service.update(ACTOR_ID, new ChecklistItemUpdateParam(
                ITEM_ID, "수정된 안전 확인", false, 1));
        service.delete(ACTOR_ID, ITEM_ID);

        verify(checklistMapper).update(any());
        verify(checklistMapper).delete(ITEM_ID, ACTOR_ID, NOW);
    }

    @Test
    void 활성_멤버는_전체_체크리스트를_조회한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(memberContext(TEAM_ID));
        ChecklistItemSearchCondition condition =
                new ChecklistItemSearchCondition(
                        PROJECT_ID, null, null, null);

        service.search(ACTOR_ID, condition);

        verify(checklistMapper).search(condition);
    }

    private ChecklistItemCreateParam projectParam() {
        return new ChecklistItemCreateParam(PROJECT_ID, null, TEAM_ID,
                ChecklistScope.PROJECT, "무대 안전 확인", true, 0);
    }

    private ChecklistItemCreateParam roundParam() {
        return new ChecklistItemCreateParam(PROJECT_ID, ROUND_ID, TEAM_ID,
                ChecklistScope.ROUND, "입장 동선 확인", true, 0);
    }

    private ChecklistItem item(boolean completed) {
        return new ChecklistItem(ITEM_ID, PROJECT_ID, null, TEAM_ID,
                ChecklistScope.PROJECT, "무대 안전 확인", true, 0,
                completed, completed ? ACTOR_ID : null,
                completed ? NOW.minusMinutes(10) : null,
                ACTOR_ID, ACTOR_ID, null, null, null);
    }

    private MemberAccessContext adminContext() {
        return new MemberAccessContext(ACTOR_ID, OTHER_TEAM_ID,
                true, false, true);
    }

    private MemberAccessContext leaderContext() {
        return new MemberAccessContext(ACTOR_ID, TEAM_ID,
                false, true, true);
    }

    private MemberAccessContext memberContext(Long teamId) {
        return new MemberAccessContext(ACTOR_ID, teamId,
                false, false, true);
    }

    private void assignId(Object target, Long value) {
        try {
            Field field = target.getClass()
                    .getDeclaredField("checklistItemId");
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
