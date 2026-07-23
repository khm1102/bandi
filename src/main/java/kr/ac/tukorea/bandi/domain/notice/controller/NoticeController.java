package kr.ac.tukorea.bandi.domain.notice.controller;

import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeDetailViewResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeManageDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeManageDetailViewResponse;
import kr.ac.tukorea.bandi.domain.notice.service.InternalNoticeService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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

    @GetMapping("/notices/manage")
    public String manage() {
        return "notice/manage-list";
    }

    @GetMapping("/notices/manage/{internalNoticeId}")
    public String manageDetail(@LoginMember Long memberId,
                               @PathVariable Long internalNoticeId, Model model) {
        InternalNoticeManageDetailResponse notice = internalNoticeService.lookupManageable(
                memberId, internalNoticeId);
        model.addAttribute("notice", InternalNoticeManageDetailViewResponse.from(notice,
                DISPLAY_DATE_TIME_FORMATTER));
        return "notice/manage-detail";
    }

    @GetMapping("/notices/{internalNoticeId}")
    public String detail(@LoginMember Long memberId, @PathVariable Long internalNoticeId,
                         Model model) {
        InternalNoticeDetailResponse notice = internalNoticeService.lookupReadable(memberId,
                internalNoticeId);
        model.addAttribute("notice", InternalNoticeDetailViewResponse.from(notice,
                DISPLAY_DATE_TIME_FORMATTER));
        return "notice/detail";
    }

    @GetMapping("/notices/{internalNoticeId}/edit")
    public String edit(@LoginMember Long memberId, @PathVariable Long internalNoticeId,
                       Model model) {
        internalNoticeService.lookupManageable(memberId, internalNoticeId);
        model.addAttribute("noticeId", internalNoticeId);
        return "notice/form";
    }
}
