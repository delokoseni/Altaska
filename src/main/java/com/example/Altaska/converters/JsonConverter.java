package com.example.Altaska.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Converter
public class JsonConverter implements AttributeConverter<Map<String, Object>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null) return null;
        try {
            System.out.println("Конвертация объекта в JSON: " + attribute);
            String jsonString = objectMapper.writeValueAsString(attribute);
            System.out.println("Результат конвертации в JSON: " + jsonString);
            return jsonString;
        } catch (JsonProcessingException e) {
            System.err.println("Ошибка при сериализации JSON: " + e.getMessage());
            throw new IllegalArgumentException("Ошибка при сериализации JSON", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            System.out.println("Получены пустые данные для десериализации. Возвращаем пустую карту.");
            return new HashMap<>();
        }
        try {
            System.out.println("Конвертация JSON в объект: " + dbData);
            Map<String, Object> result = objectMapper.readValue(dbData, Map.class);
            System.out.println("Результат десериализации: " + result);
            return result;
        } catch (IOException e) {
            System.err.println("Ошибка при десериализации JSON: " + e.getMessage());
            throw new IllegalArgumentException("Ошибка при десериализации JSON", e);
        }
    }
}
