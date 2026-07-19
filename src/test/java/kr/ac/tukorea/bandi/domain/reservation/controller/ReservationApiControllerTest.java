package kr.ac.tukorea.bandi.domain.reservation.controller;

import kr.ac.tukorea.bandi.domain.reservation.dto.request.ReservationCreateParam;
import kr.ac.tukorea.bandi.domain.reservation.dto.request.RoundSeatCreateParam;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.PublicReservationDetailResponse;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.ReservationCreatedResponse;
import kr.ac.tukorea.bandi.domain.reservation.model.ReservationStatus;
import kr.ac.tukorea.bandi.domain.reservation.service.EntryService;
import kr.ac.tukorea.bandi.domain.reservation.service.ReservationService;
import kr.ac.tukorea.bandi.domain.reservation.service.RoundSeatService;
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

@WebMvcTest({PublicReservationApiController.class,
        ReservationManagementApiController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, LoginMemberArgumentResolver.class,
        SecurityWebMvcConfig.class})
class ReservationApiControllerTest {

    private static final Long ACTOR_ID = 10L;
    private static final Long PROJECT_ID = 20L;
    private static final Long ROUND_ID = 30L;

    private final MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private RoundSeatService roundSeatService;

    @MockitoBean
    private EntryService entryService;

    @Autowired
    ReservationApiControllerTest(MockMvc mockMvc) {
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
    void 관람객이_공개_회차의_신청_가능_좌석을_조회한다() throws Exception {
        given(roundSeatService.searchAvailable("hamlet", ROUND_ID))
                .willReturn(List.of());

        mockMvc.perform(get("/api/public-reservations/{slug}/rounds/"
                        + "{roundId}/seats", "hamlet", ROUND_ID))
                .andExpect(status().isOk());

        verify(roundSeatService).searchAvailable("hamlet", ROUND_ID);
    }

    @Test
    void 관람객이_좌석을_선택해_신청한다() throws Exception {
        given(reservationService.create(any(), any())).willReturn(
                new ReservationCreatedResponse(40L, "R20260719ABC",
                        "lookup-token", "entry-token"));

        mockMvc.perform(post("/api/public-reservations/{slug}", "hamlet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationId").value(40))
                .andExpect(jsonPath("$.lookupToken").value("lookup-token"));

        verify(reservationService).create("hamlet",
                new ReservationCreateParam(ROUND_ID, List.of(41L, 42L),
                        "홍길동", "010-1234-5678", 50L));
    }

    @Test
    void 조회_토큰으로_신청을_조회하고_취소한다() throws Exception {
        given(reservationService.lookup("lookup-token")).willReturn(
                new PublicReservationDetailResponse(40L, PROJECT_ID,
                        ROUND_ID, "햄릿", "hamlet", "소극장", 1,
                        LocalDateTime.of(2026, 11, 21, 17, 0),
                        LocalDateTime.of(2026, 11, 21, 16, 30),
                        "R20260719ABC", "홍길동", "010-1234-5678",
                        ReservationStatus.CONFIRMED, null, null, true,
                        List.of()));

        mockMvc.perform(post("/api/public-reservations/lookup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lookupToken\":\"lookup-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.performanceTitle").value("햄릿"))
                .andExpect(jsonPath("$.performanceSlug").value("hamlet"))
                .andExpect(jsonPath("$.roundNo").value(1));
        mockMvc.perform(post("/api/public-reservations/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lookupToken\":\"lookup-token\","
                                + "\"reason\":\"일정 변경\"}"))
                .andExpect(status().isNoContent());

        verify(reservationService).lookup("lookup-token");
        verify(reservationService).cancel("lookup-token", "일정 변경");
    }

    @Test
    void 운영진이_회차_좌석을_등록한다() throws Exception {
        given(roundSeatService.create(any(), any())).willReturn(60L);

        mockMvc.perform(post("/api/reservation-management/seats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(seatBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(60));

        verify(roundSeatService).create(ACTOR_ID,
                new RoundSeatCreateParam(PROJECT_ID, ROUND_ID, "A-1",
                        "A", "1", "1", 0, 0, null));
    }

    @Test
    void 운영진이_회차와_상태로_신청을_조회한다() throws Exception {
        given(reservationService.search(any(), any(), any(), any(),
                any(Integer.class), any(Integer.class)))
                .willReturn(List.of());

        mockMvc.perform(get("/api/reservation-management/rounds/"
                        + "{roundId}/reservations", ROUND_ID)
                        .param("projectId", "20")
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk());

        verify(reservationService).search(ACTOR_ID, PROJECT_ID, ROUND_ID,
                ReservationStatus.CONFIRMED, 0, 20);
    }

    @Test
    void 운영진이_QR의_선택_좌석을_입장_처리한다() throws Exception {
        mockMvc.perform(post("/api/reservation-management/rounds/"
                        + "{roundId}/entry/check-ins", ROUND_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"entryToken\":\"entry-token\","
                                + "\"reservationSeatIds\":[71,72]}"))
                .andExpect(status().isNoContent());

        verify(entryService).checkIn(ACTOR_ID, ROUND_ID, "entry-token",
                List.of(71L, 72L));
    }

    @Test
    void 운영진이_신청번호와_이름으로_선택_좌석을_입장_처리한다()
            throws Exception {
        mockMvc.perform(post("/api/reservation-management/rounds/"
                        + "{roundId}/entry/manual-check-ins", ROUND_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reservationNo\":\"R20261121ABC\","
                                + "\"applicantName\":\"홍길동\","
                                + "\"reservationSeatIds\":[71,72]}"))
                .andExpect(status().isNoContent());

        verify(entryService).checkInByNumberAndName(ACTOR_ID, ROUND_ID,
                "R20261121ABC", "홍길동", List.of(71L, 72L));
    }

    @Test
    void 운영진이_잘못된_입장_처리를_사유와_함께_취소한다() throws Exception {
        mockMvc.perform(post("/api/reservation-management/rounds/"
                        + "{roundId}/entry/check-in-cancellations", ROUND_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reservationSeatId\":71,"
                                + "\"reason\":\"좌석 선택 오류\"}"))
                .andExpect(status().isNoContent());

        verify(entryService).cancelCheckIn(ACTOR_ID, ROUND_ID, 71L,
                "좌석 선택 오류");
    }

    @Test
    void 필수_관람_신청값이_없으면_C001을_반환한다() throws Exception {
        mockMvc.perform(post("/api/public-reservations/{slug}", "hamlet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    private String reservationBody() {
        return """
                {
                  "performanceRoundId": 30,
                  "performanceRoundSeatIds": [41, 42],
                  "applicantName": "홍길동",
                  "phone": "010-1234-5678",
                  "privacyPolicyVersionId": 50
                }
                """;
    }

    private String seatBody() {
        return """
                {
                  "performanceProjectId": 20,
                  "performanceRoundId": 30,
                  "seatLabel": "A-1",
                  "sectionCode": "A",
                  "rowLabel": "1",
                  "columnLabel": "1",
                  "displayRow": 0,
                  "displayColumn": 0
                }
                """;
    }
}
