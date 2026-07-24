package kr.ac.tukorea.bandi.domain.activity.controller;

import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityReportDocumentForm;
import kr.ac.tukorea.bandi.domain.activity.service.ActivityReportDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityReportDocumentService activityReportDocumentService;

    @GetMapping("/activity")
    public String activity() {
        return "activity/list";
    }

    @GetMapping("/activity/review")
    public String review() {
        return "activity/review";
    }

    @GetMapping("/activity/review/{activityRecordId}")
    public String reviewDetail(@PathVariable Long activityRecordId, Model model) {
        model.addAttribute("activityRecordId", activityRecordId);
        return "activity/review-detail";
    }

    @GetMapping("/activity/archive")
    public String archive() {
        return "activity/archive";
    }

    @GetMapping("/activity/archive/{activityRecordId}")
    public String archiveDetail(@PathVariable Long activityRecordId, Model model) {
        model.addAttribute("activityRecordId", activityRecordId);
        return "activity/archive-detail";
    }

    @GetMapping("/activity-documents")
    public String activityDocuments(Model model) {
        model.addAttribute("activityReportForm", new ActivityReportDocumentForm());
        activityReportDocumentService.lookupActivePresidentNameForPage()
                .ifPresentOrElse(
                        name -> {
                            model.addAttribute("presidentName", name);
                            model.addAttribute("presidentConfigured", true);
                        },
                        () -> model.addAttribute("presidentConfigured", false));
        return "activity/document";
    }
}
