package kr.ac.tukorea.bandi.domain.activity.controller;

import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityReportDocumentForm;
import kr.ac.tukorea.bandi.domain.activity.service.ActivityReportDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityReportDocumentService activityReportDocumentService;

    @GetMapping("/activity")
    public String activity() {
        return "activity/list";
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
