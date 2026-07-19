package kr.ac.tukorea.bandi.domain.performance.controller;

import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformancePublicPageResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicPerformanceRoundResponse;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRoundStatus;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceContentService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformancePublicNoticeService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformancePublicPageService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PerformancePageController {

    private final PerformancePublicPageService publicPageService;
    private final PerformancePublicNoticeService publicNoticeService;
    private final PerformanceRoundService roundService;
    private final PerformanceContentService contentService;

    @GetMapping("/performances/{slug}")
    public String performance(@PathVariable String slug, Model model) {
        PerformancePublicPageResponse page = publicPageService
                .lookupPublic(slug);
        List<PublicPerformanceRoundResponse> rounds = roundService
                .searchPublicRounds(slug);
        model.addAttribute("page", page);
        model.addAttribute("rounds", rounds);
        model.addAttribute("viewingGuide", publicPageService
                .lookupPublicViewingGuide(page.performanceProjectId())
                .orElse(null));
        model.addAttribute("casts", contentService
                .searchPublicCasts(slug));
        model.addAttribute("credits", contentService
                .searchPublicCredits(slug));
        model.addAttribute("media", contentService
                .searchPublicMedia(slug));
        model.addAttribute("notices", publicNoticeService
                .searchPublic(slug));
        model.addAttribute("reservationAvailable", rounds.stream()
                .anyMatch(round -> round.status()
                        == PerformanceRoundStatus.RESERVATION_OPEN));
        return "performance/public-detail";
    }
}
