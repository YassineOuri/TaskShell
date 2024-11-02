package com.example.TaskShell.commands;

import com.example.TaskShell.models.Task;
import com.example.TaskShell.models.TaskStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

@ShellComponent
public class TaskCommands {

    public TaskCommands() {
        System.out.println(" _________  ________  ________  ___  __    ________  ___       ___     \n" +
                "|\\___   ___\\\\   __  \\|\\   ____\\|\\  \\|\\  \\ |\\   ____\\|\\  \\     |\\  \\    \n" +
                "\\|___ \\  \\_\\ \\  \\|\\  \\ \\  \\___|\\ \\  \\/  /|\\ \\  \\___|\\ \\  \\    \\ \\  \\   \n" +
                "     \\ \\  \\ \\ \\   __  \\ \\_____  \\ \\   ___  \\ \\  \\    \\ \\  \\    \\ \\  \\  \n" +
                "      \\ \\  \\ \\ \\  \\ \\  \\|____|\\  \\ \\  \\\\ \\  \\ \\  \\____\\ \\  \\____\\ \\  \\ \n" +
                "       \\ \\__\\ \\ \\__\\ \\__\\____\\_\\  \\ \\__\\\\ \\__\\ \\_______\\ \\_______\\ \\__\\\n" +
                "        \\|__|  \\|__|\\|__|\\_________\\|__| \\|__|\\|_______|\\|_______|\\|__|\n" +
                "                        \\|_________|                                   ");
    }

    private StringBuilder displaySimpleList(List<Task> tasks) {
        StringBuilder output = new StringBuilder();
        for (Task task : tasks) {
            String checkString = task.getStatus() == TaskStatus.TODO ? "[ ]" : "[x]";
            String taskOutput = checkString + " " + task.getDescription() + " - Due: " + task.getDate() + "\n";
            output.append(taskOutput);
        }

        return output;
    }

    private StringBuilder displayTabularList(List<Task> tasks) {
        StringBuilder output = new StringBuilder("----------------------------------------------------\n" +
                "|  ID  |      Task Description       | Status | Due Date  |\n" +
                "----------------------------------------------------\n");
        for (Task task : tasks) {

            String taskOutput = "| " + task.getId() + " | " + task.getDescription() + " | " + task.getStatus() + " | " + task.getDate() + " |\n";
            output.append(taskOutput);
        }

        return output;
    }

    private StringBuilder displayDetailedList(List<Task> tasks) {
        StringBuilder output = new StringBuilder();
        for (Task task : tasks) {
            String taskOutput = "Task ID: " + task.getId() + "\n" +
                    "Description: " + task.getDescription() + "\n" +
                    "Status: " + task.getStatus() + "\n" +
                    "Date: " + task.getDate() + "\n" +
                    "------------------------------------------------- \n";
            output.append(taskOutput);
        }
        return output;
    }

    @ShellMethod(key = "list", value = "List tasks")
    public String listTasks(
            @ShellOption(defaultValue = "false", arity = 0, help = "Display a detailed list of tasks") Boolean d,
            @ShellOption(defaultValue = "false", arity = 0, help = "Display a table of tasks") Boolean t,
            @ShellOption(defaultValue = "no date", help = "List tasks per a given date (required format dd/mm/yyyy)") String date

    ) throws IOException {
        List<Task> tasks;
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("src/main/resources/tasks.json");

        try {


            tasks = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, Task.class));

            if (!Objects.equals(date, "no date")) {
                tasks = tasks.stream().filter(task -> Objects.equals(task.getDate(), date)).toList();
                if (tasks.isEmpty()) {
                    return "No tasks are created in this date";
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "An error occured while reading tasks, Try again later";
        }

        if (d) {
            return displayDetailedList(tasks).toString();
        }
        if (t) {
            return displayTabularList(tasks).toString();
        }

        return displaySimpleList(tasks).toString();

    }


    @ShellMethod(key = "add", value = "Create a task")
    public String createTask(
            String description,
            @ShellOption(defaultValue = "no date", help = "Create a task for a given date") String date
    ) {
        File file = new File("src/main/resources/tasks.json");
        ObjectMapper mapper = new ObjectMapper();

        try (FileWriter fileWriter = new FileWriter(file, true)) {
            Task newTask = new Task(description);

            if (file.length() == 0) {
                // File is empty, start a new array
                SequenceWriter sequenceWriter = mapper.writer().withDefaultPrettyPrinter().writeValuesAsArray(fileWriter);
                sequenceWriter.write(newTask);
                sequenceWriter.close();
            } else {
                // File has content, append to the existing JSON array
                String content = new Scanner(file).useDelimiter("\\Z").next();
                String updatedContent = content.substring(0, content.length() - 1) + ","; // Replace ']' with ','

                try (FileWriter tempWriter = new FileWriter(file)) {
                    tempWriter.write(updatedContent);
                }

                // append the new task to the array
                SequenceWriter sequenceWriter = mapper.writer().withDefaultPrettyPrinter().writeValues(fileWriter);
                sequenceWriter.write(newTask);
                fileWriter.write(']');
                sequenceWriter.close();

            }

            return "Task created successfully";
        } catch (IOException e) {
            e.printStackTrace();
            return "An error occurred while creating your task! Try again later";
        }
    }
}
