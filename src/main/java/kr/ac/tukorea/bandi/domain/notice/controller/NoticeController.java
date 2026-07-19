package kr.ac.tukorea.bandi.domain.notice.controller;

import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeSearchParam;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.service.PublicNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class NoticeController {

    private static final int PAGE_SIZE = 20;

    private final PublicNoticeService publicNoticeService;

    @GetMapping("/notices")
    public String notices(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        List<PublicNoticeSummaryResponse> notices = publicNoticeService
                .searchPublic(new PublicNoticeSearchParam(
                        keyword, page, PAGE_SIZE));
        model.addAttribute("notices", notices);
        model.addAttribute("keyword", keyword);
        model.addAttribute("page", page);
        model.addAttribute("hasPrevious", page > 0);
        model.addAttribute("hasNext", notices.size() == PAGE_SIZE);
        return "notice/list";
    }

    @GetMapping("/notices/{publicNoticeId}")
    public String notice(@PathVariable Long publicNoticeId, Model model) {
        model.addAttribute("notice", publicNoticeService
                .lookupPublic(publicNoticeId));
        return "notice/detail";
    }
}
