package kr.ac.tukorea.bandi.domain.performance.service;

import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceProjectCreateParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceProjectSearchCondition;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceProjectStatusParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceProjectUpdateParam;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceProjectResponse;
import kr.ac.tukorea.bandi.domain.performance.exception.DuplicatePerformanceTermException;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceAccessDeniedException;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceProjectNotFoundException;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformanceProjectMapper;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProject;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.lang.reflect.Field;
import java.time.LocalDate;
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
class PerformanceProjectServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long PROJECT_ID = 10L;
    private static final Long TEAM_ID = 4L;
    private static final LocalDate START_DATE = LocalDate.of(2026, 3, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 6, 30);

    @Mock
    private PerformanceProjectMapper performanceProjectMapper;
    @Mock
    private MemberService memberService;

    private PerformanceProjectService service;

    @BeforeEach
    void setUp() {
        service = new PerformanceProjectService(
                performanceProjectMapper, memberService);
    }

    @Test
    void ADMIN은_학기별_공연_프로젝트를_생성한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        willAnswer(invocation -> {
            assignId(invocation.getArgument(0), PROJECT_ID);
            return 1;
        }).given(performanceProjectMapper).insert(any());

        Long result = service.create(ACTOR_ID, createParam());

        assertThat(result).isEqualTo(PROJECT_ID);
        ArgumentCaptor<PerformanceProject> captor =
                ArgumentCaptor.forClass(PerformanceProject.class);
        verify(performanceProjectMapper).insert(captor.capture());
        assertThat(captor.getValue().getStatus())
                .isEqualTo(PerformanceProjectStatus.PLANNING);
    }

    @Test
    void ADMIN이_아니면_프로젝트를_관리할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());

        assertThatThrownBy(() -> service.create(ACTOR_ID, createParam()))
                .isInstanceOf(PerformanceAccessDeniedException.class);

        verify(performanceProjectMapper, never()).insert(any());
    }

    @Test
    void 같은_학기_프로젝트가_있으면_도메인_중복_예외로_변환한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(performanceProjectMapper.insert(any()))
                .willThrow(new DuplicateKeyException("uk_performance_term"));

        assertThatThrownBy(() -> service.create(ACTOR_ID, createParam()))
                .isInstanceOf(DuplicatePerformanceTermException.class);
    }

    @Test
    void PLANNING_프로젝트의_기본_정보를_수정한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(performanceProjectMapper.lookupByIdForUpdate(PROJECT_ID))
                .willReturn(Optional.of(project(PerformanceProjectStatus.PLANNING)));

        service.update(ACTOR_ID, new PerformanceProjectUpdateParam(
                PROJECT_ID, (short) 2026, "FIRST", "수정 공연",
                START_DATE, END_DATE, "소극장"));

        ArgumentCaptor<PerformanceProject> captor =
                ArgumentCaptor.forClass(PerformanceProject.class);
        verify(performanceProjectMapper).update(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("수정 공연");
    }

    @Test
    void 프로젝트_상태를_변경한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(performanceProjectMapper.lookupByIdForUpdate(PROJECT_ID))
                .willReturn(Optional.of(project(PerformanceProjectStatus.PLANNING)));

        service.changeStatus(ACTOR_ID, new PerformanceProjectStatusParam(
                PROJECT_ID, PerformanceProjectStatus.PRODUCING));

        ArgumentCaptor<PerformanceProject> captor =
                ArgumentCaptor.forClass(PerformanceProject.class);
        verify(performanceProjectMapper).update(captor.capture());
        assertThat(captor.getValue().getStatus())
                .isEqualTo(PerformanceProjectStatus.PRODUCING);
    }

    @Test
    void 존재하지_않는_프로젝트는_변경할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(performanceProjectMapper.lookupByIdForUpdate(PROJECT_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeStatus(ACTOR_ID,
                new PerformanceProjectStatusParam(
                        PROJECT_ID, PerformanceProjectStatus.PRODUCING)))
                .isInstanceOf(PerformanceProjectNotFoundException.class);
    }

    @Test
    void 활성_멤버는_현재와_과거_프로젝트를_조회한다() {
        PerformanceProjectSearchCondition condition =
                new PerformanceProjectSearchCondition(
                        (short) 2026, null, null, 0, 20);
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(performanceProjectMapper.search(condition))
                .willReturn(List.of(response()));
        given(performanceProjectMapper.lookupCurrent((short) 2026, "FIRST"))
                .willReturn(Optional.of(response()));

        assertThat(service.search(ACTOR_ID, condition)).hasSize(1);
        assertThat(service.lookupCurrent(ACTOR_ID, (short) 2026, "FIRST"))
                .isPresent();
    }

    @Test
    void 비활성_멤버는_프로젝트를_조회할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(new MemberAccessContext(
                        ACTOR_ID, TEAM_ID, false, false, false));

        assertThatThrownBy(() -> service.search(ACTOR_ID,
                new PerformanceProjectSearchCondition(
                        null, null, null, 0, 20)))
                .isInstanceOf(PerformanceAccessDeniedException.class);
    }

    private PerformanceProjectCreateParam createParam() {
        return new PerformanceProjectCreateParam((short) 2026, "FIRST",
                "2026 봄 정기공연", START_DATE, END_DATE, "대강당");
    }

    private PerformanceProject project(PerformanceProjectStatus status) {
        return new PerformanceProject(PROJECT_ID, (short) 2026, "FIRST",
                "2026 봄 정기공연", START_DATE, END_DATE, "대강당",
                status, ACTOR_ID, ACTOR_ID, null, null, null);
    }

    private PerformanceProjectResponse response() {
        return new PerformanceProjectResponse(PROJECT_ID, (short) 2026,
                "FIRST", "2026 봄 정기공연", START_DATE, END_DATE,
                "대강당", PerformanceProjectStatus.PLANNING,
                ACTOR_ID, ACTOR_ID, null, null);
    }

    private MemberAccessContext adminContext() {
        return new MemberAccessContext(ACTOR_ID, TEAM_ID,
                true, false, true);
    }

    private MemberAccessContext memberContext() {
        return new MemberAccessContext(ACTOR_ID, TEAM_ID,
                false, false, true);
    }

    private void assignId(Object target, Long value) {
        try {
            Field field = target.getClass()
                    .getDeclaredField("performanceProjectId");
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
