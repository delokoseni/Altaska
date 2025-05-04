package com.example.Altaska.controller;

import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.UsersRepository;
import com.example.Altaska.security.CustomUserDetails;
import com.example.Altaska.validators.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.security.Principal;
import java.util.Map;


@Controller
public class ProfileController {

    @Autowired
    private UsersRepository usersRepository;

    @GetMapping("/profile")
    public String getProfile(Model model, Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            model.addAttribute("user", userDetails);
        }
        return "profile";
    }

    @PostMapping("/profile/change-email")
    public ResponseEntity<?> changeEmail(@RequestBody Map<String, String> requestBody, Principal principal) {
        String newEmail = requestBody.get("newEmail");
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
        }
        CustomUserDetails userDetails = (CustomUserDetails) ((Authentication) principal).getPrincipal();
        Long userId = userDetails.GetUserId();
        Users user = usersRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        }
        if (newEmail == null || newEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email не может быть пустым");
        }
        newEmail = newEmail.trim();
        if (!EmailValidator.isValidLength(newEmail)) {
            return ResponseEntity.badRequest().body("Email слишком длинный (максимум " + EmailValidator.getMaxEmailLength() + " символов)");
        }
        if (!EmailValidator.isValidFormat(newEmail)) {
            return ResponseEntity.badRequest().body("Неверный формат email");
        }
        if (newEmail.equalsIgnoreCase(user.getEmail())) {
            return ResponseEntity.badRequest().body("Новый email совпадает с текущим");
        }
        user.setEmail(newEmail);
        usersRepository.save(user);
        return ResponseEntity.ok().build();
    }

}
