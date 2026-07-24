package kr.ac.tukorea.bandi.domain.guide.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GuideController {

    @GetMapping("/privacy")
    public String privacy() {
        return "guide/privacy";
    }

    @GetMapping("/support")
    public String support() {
        return "guide/support";
    }
}
