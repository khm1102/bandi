package kr.ac.tukorea.bandi.global.security;

import java.util.Locale;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class LoginViewModelAdvice {

    @ModelAttribute
    public void addLoginRole(Model model) {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication == null
                || !(authentication.getPrincipal()
                instanceof LoginPrincipal principal)) {
            return;
        }
        model.addAttribute("role",
                principal.role().toLowerCase(Locale.ROOT));
    }
}
