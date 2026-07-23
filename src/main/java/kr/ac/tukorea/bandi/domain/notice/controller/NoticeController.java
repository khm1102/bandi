package kr.ac.tukorea.bandi.domain.notice.controller;

import kr.ac.tukorea.bandi.domain.notice.service.InternalNoticeService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class NoticeController {

    private final InternalNoticeService internalNoticeService;

    @GetMapping("/notices")
    public String list() {
        return "notice/list";
    }

    @GetMapping("/notices/write")
    public String write() {
        return "notice/form";
    }

    @GetMapping("/notices/{internalNoticeId}")
    public String detail(@LoginMember Long memberId, @PathVariable Long internalNoticeId,
                         Model model) {
        model.addAttribute("notice", internalNoticeService.lookupReadable(memberId,
                internalNoticeId));
        return "notice/detail";
    }

    @GetMapping("/notices/{internalNoticeId}/edit")
    public String edit(@LoginMember Long memberId, @PathVariable Long internalNoticeId,
                       Model model) {
        model.addAttribute("notice", internalNoticeService.lookupManageable(memberId,
                internalNoticeId));
        return "notice/form";
    }
}
