package kr.ac.tukorea.bandi.domain.notice.controller;

import kr.ac.tukorea.bandi.domain.notice.service.InternalNoticeService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class NoticeController {

    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

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
        var notice = internalNoticeService.lookupReadable(memberId, internalNoticeId);
        model.addAttribute("notice", notice);
        model.addAttribute("noticePublishedAt", formatDateTime(notice.publishStartDttm()));
        model.addAttribute("noticeUpdatedAt", formatDateTime(notice.updatedDttm()));
        return "notice/detail";
    }

    @GetMapping("/notices/{internalNoticeId}/edit")
    public String edit(@LoginMember Long memberId, @PathVariable Long internalNoticeId,
                       Model model) {
        model.addAttribute("notice", internalNoticeService.lookupManageable(memberId,
                internalNoticeId));
        return "notice/form";
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : DISPLAY_DATE_TIME_FORMATTER.format(value);
    }
}
