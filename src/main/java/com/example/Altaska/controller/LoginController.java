package com.example.Altaska.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    public  LoginController() { }

    @GetMapping({"/login"})
    public String ShowLogin(HttpServletRequest request, Model model) {
        model.addAttribute("request", request);
        return "login";
    }
}
