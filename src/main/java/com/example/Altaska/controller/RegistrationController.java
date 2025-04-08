package com.example.Altaska.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RegistrationController {
    public RegistrationController() { }
    @GetMapping({"/registration"})
    public String ShowRegistration() { return "registration"; }
}
