package com.example.Altaska.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainAuthorizedController {
    @GetMapping({"/mainauthorized"})
    public String ShowLogin() { return "mainauthorized"; }
}
