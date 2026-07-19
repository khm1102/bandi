package kr.ac.tukorea.bandi.domain.performance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PerformanceContentManagementController {

    @GetMapping("/performance-content-management")
    public String management() {
        return "performance/content-management";
    }
}
