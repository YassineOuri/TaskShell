package com.example.TaskShell.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Formatter;
import java.util.UUID;


public class Task {
    private UUID id;
    private String description;
    private TaskStatus status;
    private String date;

    public Task() {
    }

    // Task with current date
    public Task(String description) {
        this.id = UUID.randomUUID();
        this.description = description;
        this.status = TaskStatus.TODO;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        this.date = LocalDate.now().format(formatter);
    }

     // Task with no status
    public Task(String description, String date) {
        this.id = UUID.randomUUID();
        this.description = description;
        this.date = date;
        this.status = TaskStatus.TODO;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
