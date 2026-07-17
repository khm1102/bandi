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
            Map.entry("/schedule", "schedule/coordination"),
            Map.entry("/resources", "resources/list"),
            Map.entry("/activity", "activity/list"),
            Map.entry("/community", "community/list"),
            Map.entry("/props", "props/list"),
            Map.entry("/reservations", "reservation/management"),
            Map.entry("/showops", "showops/operations"),
            Map.entry("/checklist", "checklist/index"),
            Map.entry("/attendance", "attendance/index"),
            Map.entry("/dues", "dues/list"),
            Map.entry("/members", "members/list"));

    @GetMapping("/login")
    public String login(@RequestParam(defaultValue = "login") String mode, Model model) {
        model.addAttribute("mode", "signup".equals(mode) ? "signup" : "login");
        return "auth/login";
    }

    @GetMapping("/reserve")
    public String reserve() {
        return "reservation/form";
    }

    @GetMapping({"/dashboard", "/calendar", "/schedule", "/resources", "/activity", "/community",
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
}
