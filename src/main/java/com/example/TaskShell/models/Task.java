package com.example.TaskShell.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Formatter;
import java.util.UUID;


public class Task implements Cloneable {
    private UUID id;
    private String description;
    private TaskStatus status;
    private String date;
    private String category;

    public Task() {
    }

    // Task with current date
    public Task(String description) {
        this.id = UUID.randomUUID();
        this.description = description;
        this.status = TaskStatus.TODO;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        this.date = LocalDate.now().format(formatter);
        this.category = "Other";
    }

     // Task with no status
    public Task(String description, String date) {
        this.id = UUID.randomUUID();
        this.description = description;
        this.date = date;
        this.status = TaskStatus.TODO;
        this.category = "Other";

    }

    public void setNewId() {
        this.id = UUID.randomUUID();
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public Task clone() {
        try {
            Task clone = (Task) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
