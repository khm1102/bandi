package kr.ac.tukorea.bandi.domain.share.controller;

import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticePublicShareResponse;
import kr.ac.tukorea.bandi.domain.notice.service.InternalNoticeService;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourcePublicShareResponse;
import kr.ac.tukorea.bandi.domain.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequiredArgsConstructor
public class ShareController {

    private static final String SHARE_DESCRIPTION = "반디 내부 게시글 · 로그인 후 내용을 확인할 수 있어요";

    private final InternalNoticeService internalNoticeService;
    private final ResourceService resourceService;

    @GetMapping("/share/notices/{shareToken}")
    public String notice(@PathVariable String shareToken, Authentication authentication,
                         Model model, HttpServletResponse response) {
        InternalNoticePublicShareResponse share = internalNoticeService.lookupPublicShare(shareToken);
        if (isAuthenticated(authentication)) {
            return "redirect:/notices/" + share.internalNoticeId();
        }
        return landing(model, response, share.title(), "/notices/" + share.internalNoticeId());
    }

    @GetMapping("/share/resources/{shareToken}")
    public String resource(@PathVariable String shareToken, Authentication authentication,
                           Model model, HttpServletResponse response) {
        ResourcePublicShareResponse share = resourceService.lookupPublicShare(shareToken);
        if (isAuthenticated(authentication)) {
            return "redirect:/resources/" + share.resourceId();
        }
        return landing(model, response, share.title(), "/resources/" + share.resourceId());
    }

    private String landing(Model model, HttpServletResponse response, String title,
                           String detailPath) {
        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("shareTitle", title);
        model.addAttribute("shareDescription", SHARE_DESCRIPTION);
        model.addAttribute("detailPath", detailPath);
        return "share/landing";
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
