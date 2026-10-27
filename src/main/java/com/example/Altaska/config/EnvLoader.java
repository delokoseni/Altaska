package com.example.Altaska.config;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvLoader {
    private EnvLoader() {
        /* This utility class should not be instantiated */
    }

    public static void load() {
        Dotenv dotenv = Dotenv.configure().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    }
}