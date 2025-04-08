package com.example.Altaska.controller;

import com.example.Altaska.models.UserType;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.UserTypeRepository;
import com.example.Altaska.repositories.UsersRepository;
import com.example.Altaska.validators.EmailValidator;
import com.example.Altaska.validators.PasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class RegistrationController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private UserTypeRepository userTypeRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public RegistrationController() { }

    @GetMapping({"/registration"})
    public String ShowRegistration() { return "registration"; }

    @PostMapping("/registration")
    public String registerUser(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("repeatpassword") String repeatpassword,
            Model model) {

        // Проверка обязательных полей на наличие значений
        if (email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                repeatpassword == null || repeatpassword.trim().isEmpty()) {
            model.addAttribute("error", "Пожалуйста, заполните все обязательные поля.");
            return "registration";
        }

        // Проверка существования email в базе данных
        if (usersRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Пользователь с таким email уже существует!");
            return "registration";
        }

        // Проверяем длину email
        if (!EmailValidator.isValidLength(email)) {
            model.addAttribute("error", "Email не должен превышать "
                    + EmailValidator.getMaxEmailLength() + " символов!");
            return "registration";
        }

        // Проверяем формат email
        if (!EmailValidator.isValidFormat(email)) {
            model.addAttribute("error", "Введите корректный адрес электронной почты!");
            return "registration";
        }

        String passwordError = PasswordValidator.validatePassword(password);
        if (passwordError != null) {
            model.addAttribute("error", passwordError);
            return "registration";
        }

        if (!password.equals(repeatpassword)) {
            model.addAttribute("error", "Пароли не совпадают!");
            return "registration"; // Вернуть на страницу регистрации с ошибкой
        }

        String hashedPassword = passwordEncoder.encode(password);
        Users newUser = new Users();
        newUser.SetEmail(email);
        newUser.SetPassword(hashedPassword);
        Optional<UserType> userTypeOptional = userTypeRepository.findByType("user");
        if (userTypeOptional.isEmpty()) {
            model.addAttribute("error", "Тип пользователя 'user' не найден в базе данных!");
            return "registration";
        }
        newUser.SetUserType(userTypeOptional.get());

        try {
            usersRepository.save(newUser);

        }catch (Exception e) {
            model.addAttribute("error", "Не удалось сохранить данные в базе данных: "
                    + e.getMessage());
        }
        return "registration";
    }
}
