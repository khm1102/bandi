package kr.ac.tukorea.bandi.domain.checklist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChecklistController {

    @GetMapping("/checklist")
    public String checklist() {
        return "checklist/index";
    }
}
