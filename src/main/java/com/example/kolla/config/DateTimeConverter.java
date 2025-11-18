package com.example.kolla.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class DateTimeConverter implements Converter<String, LocalDateTime> {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    @Override
    public LocalDateTime convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        
        source = source.trim();
        
        try {
            // Thử parse với format "dd/MM/yyyy HH:mm"
            if (source.contains(" ")) {
                return LocalDateTime.parse(source, DATE_TIME_FORMATTER);
            } else {
                // Nếu chỉ có date "dd/MM/yyyy", parse và set time thành 00:00
                return LocalDate.parse(source, DATE_FORMATTER).atStartOfDay();
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected format: dd/MM/yyyy or dd/MM/yyyy HH:mm. Received: " + source, e);
        }
    }
}

