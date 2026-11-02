package com.example.Altaska.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationManager authenticationManager;

    @GetMapping({"/login"})
    public String showLogin(HttpServletRequest request, Model model) {
        model.addAttribute("request", request);
        return "login";
    }
}
