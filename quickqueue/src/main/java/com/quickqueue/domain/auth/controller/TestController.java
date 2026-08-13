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

    @GetMapping("/api/admin/test")
    public String adminTest(Authentication authentication) {
        if (authentication == null) {
            return "authentication = null";
        }
        return "name: " + authentication.getName()
                + "\nprincipal = " + authentication.getPrincipal()
                + "\nauthenticated = " + authentication.isAuthenticated()
                + "\nauthorities = " + authentication.getAuthorities();
    }
}
