package kr.ac.tukorea.bandi.domain.notice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NoticeController {

    @GetMapping("/notices")
    public String notices() {
        return "notice/list";
    }
}
