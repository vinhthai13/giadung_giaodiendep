package com.springboot.dev_spring_boot_demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    @GetMapping("/index")
    public String index() {
        return "user/index";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) boolean checkout) {
        // Tham số checkout sẽ được tự động xử lý bởi Thymeleaf trong template
        return "user/login";
    }

    @GetMapping("/home")
    public String home() {
        return "user/index";
    }

    @GetMapping("/admin-login")
    public String admin() {
        return "redirect:/admin";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "user/access-denied";
    }
}
