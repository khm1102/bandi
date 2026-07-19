package kr.ac.tukorea.bandi.global.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import kr.ac.tukorea.bandi.domain.member.exception.MemberNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiExceptionTestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class ApiExceptionHandlerTest {

    private final MockMvc mockMvc;

    @Autowired
    ApiExceptionHandlerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void 비즈니스_예외를_ErrorCode의_HTTP_응답으로_변환한다() throws Exception {
        mockMvc.perform(get("/api/test/business"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("M001"))
                .andExpect(jsonPath("$.message").value(
                        "존재하지 않는 멤버입니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void 요청_DTO_검증_실패를_필드_오류로_변환한다() throws Exception {
        mockMvc.perform(post("/api/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
                .andExpect(jsonPath("$.fieldErrors[0].reason").value(
                        "이름을 입력해 주세요."));
    }

    @Test
    void 예상하지_못한_예외의_내부_메시지를_숨긴다() throws Exception {
        mockMvc.perform(get("/api/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("C999"))
                .andExpect(jsonPath("$.message").value(
                        "일시적인 오류가 발생했습니다."));
    }

    @Test
    void 잘못된_JSON_enum은_C001을_반환한다() throws Exception {
        mockMvc.perform(post("/api/test/enum")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INVALID\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"))
                .andExpect(jsonPath("$.message")
                        .value("입력값이 올바르지 않습니다."));
    }

    @Test
    void query_타입_변환_실패는_C001을_반환한다() throws Exception {
        mockMvc.perform(get("/api/test/query").param("memberId", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    void 필수_query_누락은_C001을_반환한다() throws Exception {
        mockMvc.perform(get("/api/test/query"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }
}

@RestController
class ApiExceptionTestController {

    @GetMapping("/api/test/business")
    void business() {
        throw new MemberNotFoundException(10L);
    }

    @PostMapping("/api/test/validation")
    void validation(@Valid @RequestBody TestRequest request) {
    }

    @GetMapping("/api/test/unexpected")
    void unexpected() {
        throw new IllegalStateException("노출되면 안 되는 내부 메시지");
    }

    @PostMapping("/api/test/enum")
    void enumRequest(@RequestBody EnumRequest request) {
    }

    @GetMapping("/api/test/query")
    void query(@RequestParam Long memberId) {
    }

    private record TestRequest(
            @NotBlank(message = "이름을 입력해 주세요.") String name
    ) {
    }

    private record EnumRequest(TestStatus status) {
    }

    private enum TestStatus {
        ACTIVE
    }
}
