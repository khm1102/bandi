package kr.ac.tukorea.bandi.global.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 디자인 시스템 데모 페이지 (docs/design-guide.md의 살아있는 예시).
 * 개발 도구이므로 dev 프로파일에서만 노출한다.
 * POST는 폼 오류 상태·flash 토스트·중복 제출 방지 데모용이다.
 */
@Controller
@Profile("dev")
public class StyleGuideController {

    @GetMapping("/style-guide")
    public String styleGuide(Model model) {
        model.addAttribute("styleGuideRequest", new StyleGuideRequest());
        return "styleguide/index";
    }

    @PostMapping("/style-guide")
    public String submitStyleGuide(@Valid @ModelAttribute StyleGuideRequest styleGuideRequest,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "styleguide/index";
        }
        redirectAttributes.addFlashAttribute("toast", "저장되었습니다.");
        return "redirect:/style-guide";
    }

    @Getter
    @Setter
    public static class StyleGuideRequest {

        @NotBlank(message = "{styleguide.name.required}")
        private String name;

        @Email(message = "{styleguide.email.invalid}")
        private String email;

        private String bio;
    }
}
