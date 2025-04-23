package com.example.Altaska.validators;

public class EmailValidator {

    private static final int MAX_EMAIL_LENGTH = 254;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    /**
     * Метод для проверки длины электронной почты
     *
     * @param email электронная почта для проверки
     * @return true, если длина допустима, иначе false
     */
    public static boolean isValidLength(String email) {
        return email != null && email.length() <= MAX_EMAIL_LENGTH;
    }

    /**
     * Метод для проверки формата электронной почты
     *
     * @param email электронная почта для проверки
     * @return true, если формат верный, иначе false
     */
    public static boolean isValidFormat(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    public static int getMaxEmailLength() {
        return MAX_EMAIL_LENGTH;
    }
}

