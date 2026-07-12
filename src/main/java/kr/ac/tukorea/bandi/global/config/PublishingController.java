package kr.ac.tukorea.bandi.global.config;

import jakarta.servlet.http.HttpServletRequest;
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
        model.addAttribute("role", ROLES.contains(role) ? role : "admin");
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return "publishing" + path;
    }
}
