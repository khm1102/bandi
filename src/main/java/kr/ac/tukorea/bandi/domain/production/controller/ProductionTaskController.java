package kr.ac.tukorea.bandi.domain.production.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProductionTaskController {

    @GetMapping("/production")
    public String production() {
        return "production/index";
    }
}
