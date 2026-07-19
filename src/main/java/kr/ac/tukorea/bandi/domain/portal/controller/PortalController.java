package kr.ac.tukorea.bandi.domain.portal.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 개발 포털 화면 라우터.
 * 기능 구현 시 화면 단위로 feature 컨트롤러에 이관하고 해당 매핑을 삭제한다.
 * role 파라미터(member|leader|admin)는 개발 중 역할별 화면 확인에만 사용한다.
 */
@Controller
@Profile("dev")
public class PortalController {

    private static final Set<String> ROLES = Set.of("member", "leader", "admin");
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
                    "반디 운영 포털은 현재 학교 시스템에서 재학생으로 확인되는 멤버만 이용할 수 있습니다."));

    /**
     * 화면별 허용 역할. 미등재 화면은 전체 역할을 허용한다.
     * layout.tag는 이 값을 모델로 전달받아 역할 전환 항목을 표시한다.
     */
    private static final Map<String, Set<String>> PAGE_ROLES = Map.of(
            "/reservations", Set.of("admin"),
            "/showops", Set.of("admin"),
            "/members", Set.of("admin"));

    private static final Map<String, String> PAGE_VIEWS = Map.ofEntries(
            Map.entry("/dashboard", "dashboard/index"),
            Map.entry("/calendar", "schedule/calendar"),
            Map.entry("/resources", "resources/list"),
            Map.entry("/activity", "activity/list"),
            Map.entry("/props", "props/list"),
            Map.entry("/reservations", "reservation/management"),
            Map.entry("/showops", "showops/operations"),
            Map.entry("/checklist", "checklist/index"),
            Map.entry("/attendance", "attendance/index"),
            Map.entry("/dues", "dues/list"),
            Map.entry("/members", "members/list"));

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, Model model) {
        LoginError loginError = error == null ? null : LOGIN_ERRORS.get(error);
        if (loginError != null) {
            model.addAttribute("loginErrorTitle", loginError.title());
            model.addAttribute("loginErrorMessage", loginError.message());
        }
        return "auth/login";
    }

    @GetMapping("/notices")
    public String notices() {
        return "notice/list";
    }

    @GetMapping("/reserve")
    public String reserve() {
        return "reservation/form";
    }

    @GetMapping({"/dashboard", "/calendar", "/resources", "/activity",
            "/props", "/reservations", "/showops", "/checklist", "/attendance", "/dues", "/members"})
    public String portalPage(HttpServletRequest request,
                             @RequestParam(defaultValue = "admin") String role,
                             Model model) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String resolvedRole = ROLES.contains(role) ? role : "admin";
        Set<String> allowedRoles = PAGE_ROLES.getOrDefault(path, ROLES);
        if (!allowedRoles.contains(resolvedRole)) {
            return "redirect:/dashboard?role=" + resolvedRole;
        }
        model.addAttribute("role", resolvedRole);
        model.addAttribute("allowedRoles", allowedRoles);
        return PAGE_VIEWS.get(path);
    }

    private record LoginError(String title, String message) {
    }
}
