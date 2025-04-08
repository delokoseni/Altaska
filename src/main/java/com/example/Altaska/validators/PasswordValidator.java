package com.example.Altaska.validators;

public class PasswordValidator {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 20;

    public static String validatePassword(String password) {
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            return "Пароль должен содержать от " + MIN_PASSWORD_LENGTH + " до " + MAX_PASSWORD_LENGTH + " символов.";
        }

        if (!password.matches(".*[a-z].*")) {
            return "Пароль должен содержать хотя бы одну строчную букву.";
        }

        if (!password.matches(".*[A-Z].*")) {
            return "Пароль должен содержать хотя бы одну заглавную букву.";
        }

        if (!password.matches(".*[0-9].*")) {
            return "Пароль должен содержать хотя бы одну цифру.";
        }

        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            return "Пароль должен содержать хотя бы один специальный символ.";
        }

        if (!password.matches("[a-zA-Z0-9!@#$%^&*(),.?\":{}|<>]+")) {
            return "Пароль может содержать только латинские буквы, цифры и специальные символы.";
        }

        return null; // Нет ошибок
    }
}