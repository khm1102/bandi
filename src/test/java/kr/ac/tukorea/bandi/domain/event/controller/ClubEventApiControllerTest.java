package kr.ac.tukorea.bandi.domain.event.controller;

import kr.ac.tukorea.bandi.domain.event.dto.request.AttendanceProcessParam;
import kr.ac.tukorea.bandi.domain.event.dto.request.ClubEventCreateParam;
import kr.ac.tukorea.bandi.domain.event.dto.request.ClubEventSearchCondition;
import kr.ac.tukorea.bandi.domain.event.dto.request.EventTargetConfirmParam;
import kr.ac.tukorea.bandi.domain.event.model.AttendanceStatus;
import kr.ac.tukorea.bandi.domain.event.model.ClubEventStatus;
import kr.ac.tukorea.bandi.domain.event.model.EventTargetScope;
import kr.ac.tukorea.bandi.domain.event.service.ClubEventService;
import kr.ac.tukorea.bandi.global.config.SecurityWebMvcConfig;
import kr.ac.tukorea.bandi.global.exception.ApiExceptionHandler;
import kr.ac.tukorea.bandi.global.security.LoginMemberArgumentResolver;
import kr.ac.tukorea.bandi.global.security.LoginPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ClubEventApiController.class, EventManagementApiController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, LoginMemberArgumentResolver.class,
        SecurityWebMvcConfig.class})
class ClubEventApiControllerTest {

    private static final Long ACTOR_ID = 10L;
    private static final Long EVENT_ID = 20L;
    private static final LocalDateTime START =
            LocalDateTime.of(2026, 8, 1, 18, 0);
    private static final LocalDateTime END =
            LocalDateTime.of(2026, 8, 1, 21, 0);

    private final MockMvc mockMvc;

    @MockitoBean
    private ClubEventService clubEventService;

    @Autowired
    ClubEventApiControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeEach
    void setUpAuthentication() {
        LoginPrincipal principal = new LoginPrincipal(ACTOR_ID, "ADMIN");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal,
                        null, principal.authorities()));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 행사를_기간과_상태로_검색한다() throws Exception {
        given(clubEventService.search(any(), any())).willReturn(List.of());

        mockMvc.perform(get("/api/events")
                        .param("status", "SCHEDULED")
                        .param("rangeStart", START.toString())
                        .param("rangeEnd", END.toString()))
                .andExpect(status().isOk());

        verify(clubEventService).search(ACTOR_ID,
                new ClubEventSearchCondition(ClubEventStatus.SCHEDULED,
                        START, END, 0, 20));
    }

    @Test
    void 관리자가_행사를_등록한다() throws Exception {
        given(clubEventService.create(any(), any())).willReturn(EVENT_ID);

        mockMvc.perform(post("/api/event-management")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clubEventId").value(EVENT_ID));

        verify(clubEventService).create(ACTOR_ID,
                new ClubEventCreateParam(EventTargetScope.ALL, null,
                        "전체 연습", "런스루", "소극장", START, END,
                        START.minusMinutes(30), START.plusMinutes(30)));
    }

    @Test
    void 선택한_행사_대상을_확정한다() throws Exception {
        given(clubEventService.confirmTargets(any(), any())).willReturn(2);

        mockMvc.perform(post("/api/event-management/{eventId}/targets", EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selectedMemberIds\":[11,12]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetCount").value(2));

        verify(clubEventService).confirmTargets(ACTOR_ID,
                new EventTargetConfirmParam(EVENT_ID, List.of(11L, 12L)));
    }

    @Test
    void 출석_상태를_일괄_처리한다() throws Exception {
        given(clubEventService.processAttendance(any(), any())).willReturn(2);

        mockMvc.perform(post("/api/event-management/{eventId}/attendances/process",
                        EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventAttendanceIds\":[31,32],"
                                + "\"status\":\"PRESENT\",\"reason\":\"현장 확인\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processedCount").value(2));

        verify(clubEventService).processAttendance(ACTOR_ID,
                new AttendanceProcessParam(EVENT_ID, List.of(31L, 32L),
                        AttendanceStatus.PRESENT, "현장 확인"));
    }

    @Test
    void 체크인을_열고_닫는다() throws Exception {
        mockMvc.perform(post("/api/event-management/{eventId}/check-in/open",
                        EVENT_ID)).andExpect(status().isNoContent());
        mockMvc.perform(post("/api/event-management/{eventId}/check-in/close",
                        EVENT_ID)).andExpect(status().isNoContent());

        verify(clubEventService).openCheckIn(ACTOR_ID, EVENT_ID);
        verify(clubEventService).closeCheckIn(ACTOR_ID, EVENT_ID);
    }

    @Test
    void 내_행사_참석_대상을_조회한다() throws Exception {
        given(clubEventService.searchMyAttendances(ACTOR_ID)).willReturn(List.of());

        mockMvc.perform(get("/api/events/my-attendances"))
                .andExpect(status().isOk());

        verify(clubEventService).searchMyAttendances(ACTOR_ID);
    }

    @Test
    void 출석_대상_ID가_없으면_C001을_반환한다() throws Exception {
        mockMvc.perform(post("/api/event-management/{eventId}/attendances/process",
                        EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventAttendanceIds\":[],\"status\":\"PRESENT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    private String eventBody() {
        return """
                {
                  "targetScope": "ALL",
                  "title": "전체 연습",
                  "description": "런스루",
                  "place": "소극장",
                  "startDttm": "2026-08-01T18:00:00",
                  "endDttm": "2026-08-01T21:00:00",
                  "checkInStartDttm": "2026-08-01T17:30:00",
                  "checkInEndDttm": "2026-08-01T18:30:00"
                }
                """;
    }
}
