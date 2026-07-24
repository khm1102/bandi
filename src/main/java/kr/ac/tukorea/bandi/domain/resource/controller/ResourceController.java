package kr.ac.tukorea.bandi.domain.resource.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ResourceController {

    @GetMapping("/resources")
    public String resources() {
        return "resources/list";
    }

    @GetMapping("/resources/write")
    public String write() {
        return "resources/form";
    }

    @GetMapping("/resources/{resourceId}")
    public String detail(@PathVariable Long resourceId) {
        return "resources/detail";
    }

    @GetMapping("/resources/{resourceId}/edit")
    public String edit(@PathVariable Long resourceId) {
        return "resources/form";
    }
}
