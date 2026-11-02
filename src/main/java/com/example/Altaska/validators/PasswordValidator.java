package com.example.Altaska.validators;

import java.util.regex.Pattern;

public class PasswordValidator {

    private static final Pattern LOWER_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern UPPER_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
    private static final Pattern ALLOWED_PATTERN = Pattern.compile("[a-zA-Z0-9!@#$%^&*(),.?\":{}|<>]+");

    private PasswordValidator() {
        /* This utility class should not be instantiated */
    }

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 20;

    public static String validatePassword(String password) {
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            return "Пароль должен содержать от " +
                    MIN_PASSWORD_LENGTH +
                    " до " +
                    MAX_PASSWORD_LENGTH +
                    " символов.";
        }

        if (!LOWER_PATTERN.matcher(password).find()) {
            return "Пароль должен содержать хотя бы одну строчную букву.";
        }

        if (!UPPER_PATTERN.matcher(password).find()) {
            return "Пароль должен содержать хотя бы одну заглавную букву.";
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            return "Пароль должен содержать хотя бы одну цифру.";
        }

        if (!SPECIAL_PATTERN.matcher(password).find()) {
            return "Пароль должен содержать хотя бы один специальный символ.";
        }

        if (!ALLOWED_PATTERN.matcher(password).find()) {
            return "Пароль может содержать только латинские буквы, цифры и специальные символы.";
        }

        return null;
    }
}