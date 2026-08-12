package com.quickqueue.domain.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/test")
    public String test(Authentication authentication) {
        return authentication.getName();
    }
}
