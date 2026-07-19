package kr.ac.tukorea.bandi.domain.fee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FeeController {

    @GetMapping("/dues")
    public String dues() {
        return "dues/list";
    }
}
