package com.example.TaskShell.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DateUtils {

    public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public DateUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String getTodayDate() {
        return LocalDate.now().format(dateTimeFormatter);
    }

    private String getTomorrowDate() {
        return LocalDate.now().plusDays(1).format(dateTimeFormatter);
    }
}
