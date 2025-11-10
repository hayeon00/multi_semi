package com.multi.travel.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Please explain the class!!!
 *
 * @author : chang
 * @filename : AuthViewController
 * @since : 2025-11-09 일요일
 */
@Controller
public class AuthViewController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

}
