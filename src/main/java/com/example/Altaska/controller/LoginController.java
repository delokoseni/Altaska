package com.example.Altaska.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    public  LoginController() { }

    @GetMapping({"/login"})
    public String ShowLogin() { return "login"; }
}
