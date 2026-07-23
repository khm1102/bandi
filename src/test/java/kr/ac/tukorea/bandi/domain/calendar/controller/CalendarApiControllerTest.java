package kr.ac.tukorea.bandi.domain.calendar.controller;

import kr.ac.tukorea.bandi.domain.calendar.dto.request.CalendarEventCreateParam;
import kr.ac.tukorea.bandi.domain.calendar.dto.request.CalendarEventSearchCondition;
import kr.ac.tukorea.bandi.domain.calendar.dto.request.CalendarEventUpdateParam;
import kr.ac.tukorea.bandi.domain.calendar.model.CalendarEventColor;
import kr.ac.tukorea.bandi.domain.calendar.service.CalendarService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalendarApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, LoginMemberArgumentResolver.class,
        SecurityWebMvcConfig.class})
class CalendarApiControllerTest {

    private static final Long ACTOR_ID = 10L;
    private static final Long EVENT_ID = 20L;
    private static final LocalDateTime START =
            LocalDateTime.of(2026, 8, 1, 10, 0);
    private static final LocalDateTime END =
            LocalDateTime.of(2026, 8, 1, 12, 0);

    private final MockMvc mockMvc;

    @MockitoBean
    private CalendarService calendarService;

    @Autowired
    CalendarApiControllerTest(MockMvc mockMvc) {
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
    void 기간과_팀으로_일정을_검색한다() throws Exception {
        given(calendarService.search(any(), any())).willReturn(List.of());

        mockMvc.perform(get("/api/calendar-events")
                        .param("rangeStart", START.toString())
                        .param("rangeEnd", END.toString())
                        .param("teamId", "3"))
                .andExpect(status().isOk());

        verify(calendarService).search(ACTOR_ID,
                new CalendarEventSearchCondition(START, END, 3L));
    }

    @Test
    void 일정을_등록한다() throws Exception {
        given(calendarService.create(any(), any())).willReturn(EVENT_ID);

        mockMvc.perform(post("/api/calendar-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.calendarEventId").value(EVENT_ID));

        verify(calendarService).create(ACTOR_ID,
                new CalendarEventCreateParam(3L, "전체 연습", "대본 리딩",
                        START, END, false, "소극장", CalendarEventColor.MINT));
    }

    @Test
    void 일정을_수정하고_삭제한다() throws Exception {
        mockMvc.perform(put("/api/calendar-events/{calendarEventId}", EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body()))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/calendar-events/{calendarEventId}",
                        EVENT_ID))
                .andExpect(status().isNoContent());

        verify(calendarService).update(ACTOR_ID,
                new CalendarEventUpdateParam(EVENT_ID, 3L, "전체 연습",
                        "대본 리딩", START, END, false, "소극장", CalendarEventColor.MINT));
        verify(calendarService).delete(ACTOR_ID, EVENT_ID);
    }

    @Test
    void 필수_일정값이_없으면_C001을_반환한다() throws Exception {
        mockMvc.perform(post("/api/calendar-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    void 제목이_150자를_넘으면_C001을_반환한다() throws Exception {
        mockMvc.perform(post("/api/calendar-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "startDttm": "2026-08-01T10:00:00",
                                  "endDttm": "2026-08-01T12:00:00",
                                  "allDay": false
                                }
                                """.formatted("가".repeat(151))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    void 허용하지_않은_표시_색상은_등록할_수_없다() throws Exception {
        mockMvc.perform(post("/api/calendar-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body().replace("\"MINT\"", "\"RAINBOW\"")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 장소와_설명_없이_일정을_등록한다() throws Exception {
        given(calendarService.create(any(), any())).willReturn(EVENT_ID);

        mockMvc.perform(post("/api/calendar-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "전체 연습",
                                  "startDttm": "2026-08-01T10:00:00",
                                  "endDttm": "2026-08-01T12:00:00",
                                  "allDay": false
                                }
                                """))
                .andExpect(status().isCreated());

        verify(calendarService).create(ACTOR_ID,
                new CalendarEventCreateParam(null, "전체 연습", null,
                        START, END, false, null));
    }

    @Test
    void 검색_종료가_시작보다_빠르면_C001을_반환한다() throws Exception {
        mockMvc.perform(get("/api/calendar-events")
                        .param("rangeStart", END.toString())
                        .param("rangeEnd", START.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    private String body() {
        return """
                {
                  "teamId": 3,
                  "title": "전체 연습",
                  "description": "대본 리딩",
                  "startDttm": "2026-08-01T10:00:00",
                  "endDttm": "2026-08-01T12:00:00",
                  "allDay": false,
                  "place": "소극장",
                  "colorCode": "MINT"
                }
                """;
    }
}
