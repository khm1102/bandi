package kr.ac.tukorea.bandi.domain.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberController {

    @GetMapping("/members")
    public String members() {
        return "members/list";
    }
}
