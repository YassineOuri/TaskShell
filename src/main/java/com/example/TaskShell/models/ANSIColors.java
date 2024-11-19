package com.example.TaskShell.models;

public class ANSIColors {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BLUE = "\u001B[34m";

    public static String greenText(String text) {
        return ANSI_GREEN + text + ANSI_RESET;
    }

    public static String redText(String text) {
        return ANSI_RED + text + ANSI_RESET;
    }
}
