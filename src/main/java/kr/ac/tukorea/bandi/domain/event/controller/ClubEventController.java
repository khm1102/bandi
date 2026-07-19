package kr.ac.tukorea.bandi.domain.event.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClubEventController {

    @GetMapping("/attendance")
    public String attendance() {
        return "attendance/index";
    }
}
