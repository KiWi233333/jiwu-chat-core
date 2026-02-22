package com.jiwu.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.jiwu.api.common.annotation.IgnoreAuth;

/**
 * 首页控制器
 */
@Controller
public class HomeController {

    @GetMapping({"/", "/index"})
    @IgnoreAuth
    public String index(Model model) {
        model.addAttribute("appName", "JiwuChat");
        model.addAttribute("appDesc", "集成社区、商城、IM与AI的下一代模块化平台");
        return "index";
    }
}
