package com.example.Altaska.controller;

import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.UsersRepository;
import com.example.Altaska.security.CustomUserDetails;
import com.example.Altaska.services.EmailService;
import com.example.Altaska.validators.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;


@Controller
public class ProfileController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private EmailService emailService;

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
            return ResponseEntity.badRequest().body("Email слишком длинный");
        }

        if (!EmailValidator.isValidFormat(newEmail)) {
            return ResponseEntity.badRequest().body("Неверный формат email");
        }

        if (newEmail.equalsIgnoreCase(user.getEmail())) {
            return ResponseEntity.badRequest().body("Новый email совпадает с текущим");
        }

        // Генерация токенов
        String oldToken = java.util.UUID.randomUUID().toString();
        String newToken = java.util.UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

        user.setNewEmail(newEmail);
        user.setOldEmailChangeToken(oldToken);
        user.setOldEmailChangeTokenExpiresAt(expiresAt);
        user.setNewEmailChangeToken(newToken);
        user.setNewEmailChangeTokenExpiresAt(expiresAt);
        user.setEmailChangeStatus(null);

        usersRepository.save(user);

        String confirmOldUrl = "http://localhost:8080/confirm-old?token=" + oldToken;
        String confirmNewUrl = "http://localhost:8080/confirm-new?token=" + newToken;

        emailService.sendEmail(user.getEmail(), "Подтверждение смены email (старый)",
                "Перейдите по ссылке, чтобы подтвердить смену email: " + confirmOldUrl);
        emailService.sendEmail(newEmail, "Подтверждение смены email (новый)",
                "Перейдите по ссылке, чтобы подтвердить смену email: " + confirmNewUrl);

        return ResponseEntity.ok("Письма отправлены на оба адреса");
    }

    @GetMapping("/confirm-old")
    public String confirmOldEmail(@RequestParam("token") String token, Model model) {
        Users user = usersRepository.findByOldEmailChangeToken(token).orElse(null);
        if (user == null || user.getOldEmailChangeTokenExpiresAt().isBefore(LocalDateTime.now())) {
            model.addAttribute("statusTitle", "Ошибка");
            model.addAttribute("statusMessage", "Ссылка недействительна или устарела.");
            model.addAttribute("goToLogin", true);
            return "confirmInvite";
        }

        if (user.getEmailChangeStatus() == null) {
            user.setEmailChangeStatus(false);
        } else if (!user.getEmailChangeStatus()) {
            completeEmailChange(user);
        }

        usersRepository.save(user);
        model.addAttribute("statusTitle", "Email подтверждён");
        model.addAttribute("statusMessage", "Старый email успешно подтверждён.");
        model.addAttribute("goToLogin", true);
        return "confirmInvite";
    }

    @GetMapping("/confirm-new")
    public String confirmNewEmail(@RequestParam("token") String token, Model model) {
        Users user = usersRepository.findByNewEmailChangeToken(token).orElse(null);
        if (user == null || user.getNewEmailChangeTokenExpiresAt().isBefore(LocalDateTime.now())) {
            model.addAttribute("statusTitle", "Ошибка");
            model.addAttribute("statusMessage", "Ссылка недействительна или устарела.");
            model.addAttribute("goToLogin", true);
            return "confirmInvite";
        }

        if (user.getEmailChangeStatus() == null) {
            user.setEmailChangeStatus(false);
        } else if (!user.getEmailChangeStatus()) {
            completeEmailChange(user);
        }

        usersRepository.save(user);
        model.addAttribute("statusTitle", "Email подтверждён");
        model.addAttribute("statusMessage", "Новый email успешно подтверждён.");
        model.addAttribute("goToLogin", true);
        return "confirmInvite";
    }

    private void completeEmailChange(Users user) {
        user.setEmail(user.getNewEmail());
        user.setNewEmail(null);
        user.setOldEmailChangeToken(null);
        user.setOldEmailChangeTokenExpiresAt(null);
        user.setNewEmailChangeToken(null);
        user.setNewEmailChangeTokenExpiresAt(null);
        user.setEmailChangeStatus(null);
    }

}
