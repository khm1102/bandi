package kr.ac.tukorea.bandi.domain.notice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PublicNoticeManagementController {

    @GetMapping("/notice-management")
    public String list() {
        return "notice/management-list";
    }

    @GetMapping("/notice-management/write")
    public String write(Model model) {
        model.addAttribute("publicNoticeId", null);
        return "notice/management-editor";
    }

    @GetMapping("/notice-management/{publicNoticeId}/edit")
    public String edit(@PathVariable Long publicNoticeId, Model model) {
        model.addAttribute("publicNoticeId", publicNoticeId);
        return "notice/management-editor";
    }
}
