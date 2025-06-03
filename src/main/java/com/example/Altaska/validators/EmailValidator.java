package com.example.Altaska.validators;

public class EmailValidator {
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static boolean isValidLength(String email) {
        return email != null && email.length() <= MAX_EMAIL_LENGTH;
    }
    public static boolean isValidFormat(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }
    public static int getMaxEmailLength() {
        return MAX_EMAIL_LENGTH;
    }
}

