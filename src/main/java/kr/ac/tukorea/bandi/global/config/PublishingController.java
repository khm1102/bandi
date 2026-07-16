package kr.ac.tukorea.bandi.global.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 화면 퍼블리싱 임시 컨트롤러 (dev 전용) — 목업 전 화면을 정적 JSP로 서빙한다.
 * 기능 구현 시 화면 단위로 feature 컨트롤러로 이관하고 해당 매핑을 삭제한다.
 * role 파라미터(member|leader|admin)는 퍼블리싱 미리보기용 노출 분기다.
 */
@Controller
@Profile("dev")
public class PublishingController {

    private static final Set<String> ROLES = Set.of("member", "leader", "admin");

    /** 화면별 허용 역할 — 미등재 화면은 전체 역할 허용. layout.tag 사이드바의 노출 조건과 같은 정책이다. */
    private static final Map<String, Set<String>> PAGE_ROLES = Map.of(
            "/reservations", Set.of("admin"),
            "/showops", Set.of("admin"),
            "/members", Set.of("leader", "admin"));

    @GetMapping("/login")
    public String login(@RequestParam(defaultValue = "login") String mode, Model model) {
        model.addAttribute("mode", "signup".equals(mode) ? "signup" : "login");
        return "publishing/login";
    }

    @GetMapping("/reserve")
    public String reserve() {
        return "publishing/reserve";
    }

    @GetMapping({"/dashboard", "/calendar", "/schedule", "/resources", "/activity", "/community",
            "/props", "/reservations", "/showops", "/checklist", "/attendance", "/dues", "/members"})
    public String adminPage(HttpServletRequest request,
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
        return "publishing" + path;
    }
}
