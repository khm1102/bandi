package kr.ac.tukorea.bandi.domain.member.controller;

import kr.ac.tukorea.bandi.domain.member.dto.request.CohortChangeParam;
import kr.ac.tukorea.bandi.domain.member.dto.request.MemberPreRegisterParam;
import kr.ac.tukorea.bandi.domain.member.dto.request.MemberSearchCondition;
import kr.ac.tukorea.bandi.domain.member.dto.request.RoleChangeParam;
import kr.ac.tukorea.bandi.domain.member.dto.request.StatusChangeParam;
import kr.ac.tukorea.bandi.domain.member.dto.request.TeamChangeParam;
import kr.ac.tukorea.bandi.domain.member.dto.response.MemberHistoryResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.MemberResponse;
import kr.ac.tukorea.bandi.domain.member.model.AcademicStatus;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, LoginMemberArgumentResolver.class,
        SecurityWebMvcConfig.class})
class MemberApiControllerTest {

    private static final Long ACTOR_ID = 10L;
    private static final Long MEMBER_ID = 20L;

    private final MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Autowired
    MemberApiControllerTest(MockMvc mockMvc) {
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
    void 멤버_목록_검색_조건을_Service에_전달한다() throws Exception {
        given(memberService.searchMembers(any())).willReturn(List.of(member()));

        mockMvc.perform(get("/api/members")
                        .param("keyword", "서준")
                        .param("teamId", "2")
                        .param("status", "ACTIVE")
                        .param("role", "MEMBER")
                        .param("ssoLinkStatus", "LINKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].memberId").value(MEMBER_ID))
                .andExpect(jsonPath("$[0].studentNo")
                        .value("2020184000"));

        verify(memberService).searchMembers(new MemberSearchCondition(
                "서준", 2L, MemberStatus.ACTIVE, ClubRole.MEMBER,
                SsoLinkStatus.LINKED));
    }

    @Test
    void 멤버_상세와_이력을_조회한다() throws Exception {
        given(memberService.lookupMember(MEMBER_ID)).willReturn(member());
        given(memberService.lookupMemberHistory(MEMBER_ID))
                .willReturn(new MemberHistoryResponse(List.of(), List.of(),
                        List.of(), List.of()));

        mockMvc.perform(get("/api/members/{memberId}", MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("이서준"));
        mockMvc.perform(get("/api/members/{memberId}/histories", MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamHistories").isArray());
    }

    @Test
    void 멤버를_사전_등록하고_생성_위치를_반환한다() throws Exception {
        given(memberService.preRegister(any(), any())).willReturn(MEMBER_ID);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentNo": "2026184000",
                                  "name": "김하늘",
                                  "teamId": 2,
                                  "cohortId": 3
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        "/api/members/" + MEMBER_ID))
                .andExpect(jsonPath("$.memberId").value(MEMBER_ID));

        verify(memberService).preRegister(ACTOR_ID,
                new MemberPreRegisterParam("2026184000", "김하늘", 2L,
                        3L));
    }

    @Test
    void 빈_등록_요청은_C001을_반환한다() throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"))
                .andExpect(jsonPath("$.fieldErrors.length()").value(4));
    }

    @Test
    void 팀_변경에_path_멤버와_세션_처리자를_사용한다() throws Exception {
        mockMvc.perform(patch("/api/members/{memberId}/team", MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newTeamId": 5, "reason": "팀 재배치"}
                                """))
                .andExpect(status().isNoContent());

        verify(memberService).changeTeam(ACTOR_ID,
                new TeamChangeParam(MEMBER_ID, 5L, "팀 재배치"));
    }

    @Test
    void 기수_권한_상태_변경을_Service에_전달한다() throws Exception {
        mockMvc.perform(patch("/api/members/{memberId}/cohort", MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newCohortId": 6, "reason": "기수 정정"}
                                """))
                .andExpect(status().isNoContent());
        mockMvc.perform(patch("/api/members/{memberId}/role", MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newRole": "LEADER", "reason": "팀장 지정"}
                                """))
                .andExpect(status().isNoContent());
        mockMvc.perform(patch("/api/members/{memberId}/status", MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newStatus": "SUSPENDED", "reason": "활동 중지"}
                                """))
                .andExpect(status().isNoContent());

        verify(memberService).changeCohort(ACTOR_ID,
                new CohortChangeParam(MEMBER_ID, 6L, "기수 정정"));
        verify(memberService).changeRole(ACTOR_ID,
                new RoleChangeParam(MEMBER_ID, ClubRole.LEADER, "팀장 지정"));
        verify(memberService).changeStatus(ACTOR_ID,
                new StatusChangeParam(MEMBER_ID, MemberStatus.SUSPENDED,
                        "활동 중지"));
    }

    @Test
    void 빈_변경_사유는_C001을_반환한다() throws Exception {
        mockMvc.perform(patch("/api/members/{memberId}/team", MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newTeamId": 5, "reason": " "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    void 팀과_기수_기준정보를_조회한다() throws Exception {
        given(memberService.searchTeams(true)).willReturn(List.of());
        given(memberService.searchCohorts(true)).willReturn(List.of());

        mockMvc.perform(get("/api/members/reference/teams")
                        .param("activeOnly", "true"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/members/reference/cohorts")
                        .param("activeOnly", "true"))
                .andExpect(status().isOk());

        verify(memberService).searchTeams(true);
        verify(memberService).searchCohorts(true);
    }

    private MemberResponse member() {
        return new MemberResponse(MEMBER_ID, "2020184000", "이서준",
                "컴퓨터공학부", AcademicStatus.ENROLLED, null, 2L, 3L,
                ClubRole.MEMBER, MemberStatus.ACTIVE, SsoLinkStatus.LINKED,
                null, null, ACTOR_ID);
    }
}
