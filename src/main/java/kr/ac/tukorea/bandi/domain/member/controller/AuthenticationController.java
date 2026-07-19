package kr.ac.tukorea.bandi.domain.member.controller;

import kr.ac.tukorea.bandi.domain.member.dto.request.SchoolLoginForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class AuthenticationController {

    private static final Map<String, LoginError> LOGIN_ERRORS = Map.of(
            "school-unavailable", new LoginError(
                    "학교 로그인 서비스 장애",
                    "학교 시스템에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요."),
            "bad-credentials", new LoginError(
                    "학교 계정 확인 필요",
                    "학교 포털 아이디와 비밀번호를 다시 확인해 주세요."),
            "member-not-registered", new LoginError(
                    "멤버 사전 등록 필요",
                    "운영진이 학번을 먼저 등록해야 합니다. 동아리 운영진에게 문의해 주세요."),
            "link-pending", new LoginError(
                    "학교 계정 연결 대기",
                    "학교 계정 확인은 완료됐지만 멤버 연결이 아직 처리되지 않았습니다."),
            "academic-restricted", new LoginError(
                    "학적 상태 확인 필요",
                    "반디 운영 포털은 현재 학교 시스템에서 재학생으로 확인되는 멤버만 이용할 수 있습니다."),
            "member-restricted", new LoginError(
                    "멤버 이용 상태 확인 필요",
                    "현재 멤버 상태로는 로그인할 수 없습니다. 동아리 운영진에게 문의해 주세요."));

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        Model model) {
        model.addAttribute("schoolLoginForm", new SchoolLoginForm());
        LoginError loginError = error == null ? null : LOGIN_ERRORS.get(error);
        if (loginError != null) {
            model.addAttribute("loginErrorTitle", loginError.title());
            model.addAttribute("loginErrorMessage", loginError.message());
        }
        return "auth/login";
    }

    private record LoginError(String title, String message) {
    }
}
