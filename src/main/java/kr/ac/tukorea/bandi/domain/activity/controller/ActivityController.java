package kr.ac.tukorea.bandi.domain.activity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ActivityController {

    @GetMapping("/activity")
    public String activity() {
        return "activity/list";
    }
}
