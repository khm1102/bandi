package kr.ac.tukorea.bandi.domain.asset.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AssetController {

    @GetMapping("/props")
    public String assets() {
        return "props/list";
    }

    @GetMapping("/props/new")
    public String create() {
        return "props/form";
    }

    @GetMapping("/props/{assetItemId}")
    public String detail(@PathVariable Long assetItemId) {
        return "props/detail";
    }

    @GetMapping("/props/{assetItemId}/edit")
    public String edit(@PathVariable Long assetItemId) {
        return "props/form";
    }
}
