package kr.ac.tukorea.bandi.domain.production.service;

import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceProjectService;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskCreateParam;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskSearchCondition;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskStatusParam;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskUpdateParam;
import kr.ac.tukorea.bandi.domain.production.dto.response.ProductionProgressResponse;
import kr.ac.tukorea.bandi.domain.production.exception.ProductionAccessDeniedException;
import kr.ac.tukorea.bandi.domain.production.exception.ProductionTaskNotFoundException;
import kr.ac.tukorea.bandi.domain.production.mapper.ProductionTaskMapper;
import kr.ac.tukorea.bandi.domain.production.model.ProductionTask;
import kr.ac.tukorea.bandi.domain.production.model.ProductionTaskHistory;
import kr.ac.tukorea.bandi.domain.production.model.ProductionTaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
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
class ProductionTaskServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long PROJECT_ID = 10L;
    private static final Long TASK_ID = 20L;
    private static final Long TEAM_ID = 4L;
    private static final Long OTHER_TEAM_ID = 5L;
    private static final LocalDate START_DATE = LocalDate.of(2026, 3, 1);
    private static final LocalDate DUE_DATE = LocalDate.of(2026, 3, 31);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-03-15T03:00:00Z"),
            ZoneId.of("Asia/Seoul"));

    @Mock
    private ProductionTaskMapper productionTaskMapper;
    @Mock
    private MemberService memberService;
    @Mock
    private PerformanceProjectService performanceProjectService;

    private ProductionTaskService service;

    @BeforeEach
    void setUp() {
        service = new ProductionTaskService(productionTaskMapper,
                memberService, performanceProjectService, CLOCK);
    }

    @Test
    void 활성_멤버는_소속_팀_업무를_생성한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        willAnswer(invocation -> {
            assignId(invocation.getArgument(0), TASK_ID);
            return 1;
        }).given(productionTaskMapper).insert(any());

        Long result = service.create(ACTOR_ID, createParam(TEAM_ID));

        assertThat(result).isEqualTo(TASK_ID);
        verify(memberService).validateActiveTeam(TEAM_ID);
        verify(performanceProjectService)
                .validateProductionMutable(ACTOR_ID, PROJECT_ID);
    }

    @Test
    void 일반_멤버와_팀장은_다른_팀_업무를_생성할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(memberContext(), leaderContext());

        assertThatThrownBy(() -> service.create(
                ACTOR_ID, createParam(OTHER_TEAM_ID)))
                .isInstanceOf(ProductionAccessDeniedException.class);
        assertThatThrownBy(() -> service.create(
                ACTOR_ID, createParam(OTHER_TEAM_ID)))
                .isInstanceOf(ProductionAccessDeniedException.class);

        verify(productionTaskMapper, never()).insert(any());
    }

    @Test
    void 운영진은_다른_팀_업무도_생성한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());

        service.create(ACTOR_ID, createParam(OTHER_TEAM_ID));

        verify(productionTaskMapper).insert(any());
    }

    @Test
    void 팀장과_운영진만_업무_기본_정보를_수정한다() {
        given(productionTaskMapper.lookupByIdForUpdate(TASK_ID))
                .willReturn(Optional.of(task()));
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());

        assertThatThrownBy(() -> service.update(ACTOR_ID, updateParam()))
                .isInstanceOf(ProductionAccessDeniedException.class);

        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(leaderContext());
        service.update(ACTOR_ID, updateParam());
        verify(productionTaskMapper).update(any());
    }

    @Test
    void 소속_팀_멤버는_상태를_변경하고_이력을_남긴다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(productionTaskMapper.lookupByIdForUpdate(TASK_ID))
                .willReturn(Optional.of(task()));

        service.changeStatus(ACTOR_ID, new ProductionTaskStatusParam(
                TASK_ID, ProductionTaskStatus.IN_PROGRESS,
                null, "작업 시작"));

        ArgumentCaptor<ProductionTask> taskCaptor =
                ArgumentCaptor.forClass(ProductionTask.class);
        verify(productionTaskMapper).update(taskCaptor.capture());
        assertThat(taskCaptor.getValue().getStatus())
                .isEqualTo(ProductionTaskStatus.IN_PROGRESS);
        ArgumentCaptor<ProductionTaskHistory> historyCaptor =
                ArgumentCaptor.forClass(ProductionTaskHistory.class);
        verify(productionTaskMapper).insertHistory(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getComment()).isEqualTo("작업 시작");
    }

    @Test
    void 다른_팀_멤버는_상태를_변경할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(new MemberAccessContext(
                        ACTOR_ID, OTHER_TEAM_ID, false, false, true));
        given(productionTaskMapper.lookupByIdForUpdate(TASK_ID))
                .willReturn(Optional.of(task()));

        assertThatThrownBy(() -> service.changeStatus(ACTOR_ID,
                new ProductionTaskStatusParam(TASK_ID,
                        ProductionTaskStatus.COMPLETED, null, null)))
                .isInstanceOf(ProductionAccessDeniedException.class);
    }

    @Test
    void 팀장과_운영진만_업무를_논리_삭제한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(leaderContext());
        given(productionTaskMapper.lookupByIdForUpdate(TASK_ID))
                .willReturn(Optional.of(task()));

        service.delete(ACTOR_ID, TASK_ID);

        verify(productionTaskMapper).delete(
                org.mockito.ArgumentMatchers.eq(TASK_ID),
                org.mockito.ArgumentMatchers.eq(ACTOR_ID), any());
    }

    @Test
    void 종료되지_않은_프로젝트인지_모든_쓰기에서_검증한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(productionTaskMapper.lookupByIdForUpdate(TASK_ID))
                .willReturn(Optional.of(task()));

        service.create(ACTOR_ID, createParam(TEAM_ID));
        service.update(ACTOR_ID, updateParam());
        service.changeStatus(ACTOR_ID, new ProductionTaskStatusParam(
                TASK_ID, ProductionTaskStatus.IN_PROGRESS, null, null));
        service.delete(ACTOR_ID, TASK_ID);

        org.mockito.Mockito.verify(performanceProjectService,
                org.mockito.Mockito.times(4))
                .validateProductionMutable(ACTOR_ID, PROJECT_ID);
    }

    @Test
    void 활성_멤버는_전체_업무와_진행률과_이력을_조회한다() {
        ProductionTaskSearchCondition condition =
                new ProductionTaskSearchCondition(
                        PROJECT_ID, null, null, false, 0, 20);
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(productionTaskMapper.lookupProjectProgress(
                PROJECT_ID, LocalDate.of(2026, 3, 15)))
                .willReturn(progress(null));

        assertThat(service.search(ACTOR_ID, condition)).isEmpty();
        assertThat(service.lookupProjectProgress(ACTOR_ID, PROJECT_ID)
                .totalCount()).isZero();
        assertThat(service.searchTeamProgress(ACTOR_ID, PROJECT_ID)).isEmpty();
        assertThat(service.searchHistories(ACTOR_ID, TASK_ID)).isEmpty();
    }

    @Test
    void 존재하지_않는_업무는_변경할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(productionTaskMapper.lookupByIdForUpdate(TASK_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(ACTOR_ID, TASK_ID))
                .isInstanceOf(ProductionTaskNotFoundException.class);
    }

    private ProductionTaskCreateParam createParam(Long teamId) {
        return new ProductionTaskCreateParam(PROJECT_ID, teamId,
                "무대 도면 확정", "최종 치수 반영", START_DATE, DUE_DATE);
    }

    private ProductionTaskUpdateParam updateParam() {
        return new ProductionTaskUpdateParam(TASK_ID, "수정 업무",
                "수정 설명", START_DATE, DUE_DATE);
    }

    private ProductionTask task() {
        return new ProductionTask(TASK_ID, PROJECT_ID, TEAM_ID,
                "무대 도면 확정", "최종 치수 반영", START_DATE, DUE_DATE,
                ProductionTaskStatus.TODO, null, ACTOR_ID, ACTOR_ID,
                null, null, null);
    }

    private ProductionProgressResponse progress(Long teamId) {
        return new ProductionProgressResponse(
                teamId, null, 0, 0, 0, 0);
    }

    private MemberAccessContext memberContext() {
        return new MemberAccessContext(ACTOR_ID, TEAM_ID,
                false, false, true);
    }

    private MemberAccessContext leaderContext() {
        return new MemberAccessContext(ACTOR_ID, TEAM_ID,
                false, true, true);
    }

    private MemberAccessContext adminContext() {
        return new MemberAccessContext(ACTOR_ID, TEAM_ID,
                true, false, true);
    }

    private void assignId(Object target, Long value) {
        try {
            Field field = target.getClass().getDeclaredField("productionTaskId");
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
