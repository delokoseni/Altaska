package com.example.Altaska.controller;

import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.UsersRepository;
import com.example.Altaska.security.CustomUserDetails;
import com.example.Altaska.services.EmailService;
import com.example.Altaska.validators.EmailValidator;
import com.example.Altaska.validators.PasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;


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
            user.setOldEmailChangeToken(null);
            user.setOldEmailChangeTokenExpiresAt(null);
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
            user.setNewEmailChangeToken(null);
            user.setNewEmailChangeTokenExpiresAt(null);
        } else if (!user.getEmailChangeStatus()) {
            completeEmailChange(user);
        }

        usersRepository.save(user);
        model.addAttribute("statusTitle", "Email подтверждён");
        model.addAttribute("statusMessage", "Новый email успешно подтверждён.");
        model.addAttribute("goToLogin", true);
        return "confirmInvite";
    }

    @Autowired
    private SessionRegistry sessionRegistry;

    private void expireUserSessions(String email) {
        List<Object> principals = sessionRegistry.getAllPrincipals();

        for (Object principal : principals) {
            if (principal instanceof CustomUserDetails userDetails) {
                if (userDetails.getUsername().equals(email)) {
                    for (SessionInformation session : sessionRegistry.getAllSessions(principal, false)) {
                        session.expireNow();
                    }
                }
            }
        }
    }

    private void completeEmailChange(Users user) {
        expireUserSessions(user.getEmail());
        user.setEmail(user.getNewEmail());
        user.setNewEmail(null);
        user.setEmailChangeStatus(null);
    }

    @PostMapping("/profile/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> requestBody, Principal principal) {
        String oldPassword = requestBody.get("oldPassword");
        String newPassword = requestBody.get("newPassword");
        String repeatPassword = requestBody.get("repeatPassword");

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
        }

        CustomUserDetails userDetails = (CustomUserDetails) ((Authentication) principal).getPrincipal();
        Long userId = userDetails.GetUserId();
        Users user = usersRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        if (!encoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body("Неверный старый пароль");
        }

        if (newPassword == null || repeatPassword == null || !newPassword.equals(repeatPassword)) {
            return ResponseEntity.badRequest().body("Новые пароли не совпадают или пустые");
        }

        if (encoder.matches(newPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body("Новый пароль совпадает со старым");
        }

        String validationError = PasswordValidator.validatePassword(newPassword);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        // Сохраняем закодированный новый пароль временно
        String encodedNewPassword = encoder.encode(newPassword);
        String confirmToken = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

        user.setPasswordResetToken(confirmToken);
        user.setPasswordResetTokenExpiresAt(expiresAt);
        user.setNewPassword(encodedNewPassword); // временно используем confirmationToken для хранения

        usersRepository.save(user);

        String confirmUrl = "http://localhost:8080/confirm-password-change?token=" + confirmToken;
        emailService.sendEmail(user.getEmail(), "Подтверждение смены пароля",
                "Для завершения смены пароля перейдите по ссылке: " + confirmUrl);

        return ResponseEntity.ok("Письмо с подтверждением отправлено на почту");
    }

    @GetMapping("/confirm-password-change")
    public String confirmPasswordChange(@RequestParam("token") String token, Model model) {
        Users user = usersRepository.findByPasswordResetToken(token).orElse(null);
        if (user == null || user.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            model.addAttribute("statusTitle", "Ошибка");
            model.addAttribute("statusMessage", "Ссылка для подтверждения недействительна или устарела.");
            model.addAttribute("goToLogin", true);
            return "confirmInvite";
        }
        user.setPassword(user.getNewPassword());
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);
        user.setNewPassword(null);

        usersRepository.save(user);

        model.addAttribute("statusTitle", "Пароль обновлён");
        model.addAttribute("statusMessage", "Пароль успешно изменён. Теперь вы можете войти с новым паролем.");
        model.addAttribute("goToLogin", true);
        return "confirmInvite";
    }

}
