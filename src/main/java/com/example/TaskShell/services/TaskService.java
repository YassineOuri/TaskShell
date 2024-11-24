package com.example.TaskShell.services;

import com.example.TaskShell.exceptions.EmplyTaskListException;
import com.example.TaskShell.models.ANSIColors;
import com.example.TaskShell.models.Task;
import com.example.TaskShell.models.TaskStatus;
import com.example.TaskShell.utils.DateUtils;
import com.example.TaskShell.utils.TaskUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

@Service
public class TaskService {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Lists tasks based on the given parameters and file contents.
     *
     * @param displayAll whether to display all tasks
     * @param isDetailed whether to display tasks in detailed format
     * @param isTable    whether to display tasks in tabular format
     * @param date       the date to filter tasks
     * @param file       the file containing tasks
     * @return a formatted string representation of tasks
     * @throws IOException               if an error occurs during file operations
     * @throws EmplyTaskListException    if no tasks are found for the specified conditions
     */
    public String listTasks(boolean displayAll, boolean isDetailed, boolean isTable, String date, File file) {
        try {
            List<Task> tasks = readTasksFromFile(file);

            // Filter tasks based on date conditions
            tasks = filterTasks(tasks, displayAll, date);

            // Format tasks based on the desired output style
            if (isDetailed) {
                return TaskUtils.displayDetailedList(tasks).toString();
            } else if (isTable) {
                return TaskUtils.displayTabularList(tasks).toString();
            } else {
                String dueDate = Objects.equals(date, "no date") ? DateUtils.getTodayDate() : date;
                return TaskUtils.displaySimpleList(tasks, dueDate).toString();
            }
        } catch (EmplyTaskListException e) {
            return ANSIColors.redText(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return ANSIColors.redText("An error occurred while reading tasks. Try again later.");
        }
    }

    /**
     * Adds a new task to the specified file.
     *
     * @param file        the file containing tasks
     * @param description the description of the new task
     * @param date        the due date of the new task
     * @param status      the status of the new task
     */
    public void addNewTask(File file, String description, String date, String status) {
        try (FileWriter fileWriter = new FileWriter(file, true)) {
            Task newTask = createTask(description, date, status);

            if (file.length() == 0) {
                // File is empty, start a new JSON array
                try (SequenceWriter sequenceWriter = mapper.writer().withDefaultPrettyPrinter().writeValuesAsArray(fileWriter)) {
                    sequenceWriter.write(newTask);
                }
            } else {
                // Append to an existing JSON array
                appendTaskToFile(file, newTask);
            }
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while adding the task: " + e.getMessage(), e);
        }
    }

    // Private Helper Methods

    /**
     * Reads tasks from the specified file.
     *
     * @param file the file containing tasks
     * @return a list of tasks
     * @throws IOException if an error occurs during file reading
     */
    private List<Task> readTasksFromFile(File file) throws IOException {
        return mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, Task.class));
    }

    /**
     * Filters tasks based on display options and date.
     *
     * @param tasks      the list of tasks to filter
     * @param displayAll whether to display all tasks
     * @param date       the date to filter tasks
     * @return a filtered list of tasks
     * @throws EmplyTaskListException if no tasks match the filter conditions
     */
    private List<Task> filterTasks(List<Task> tasks, boolean displayAll, String date) throws EmplyTaskListException {
        if (!displayAll) {
            if (Objects.equals(date, "no date")) {
                tasks = tasks.stream()
                        .filter(task -> Objects.equals(task.getDate(), DateUtils.getTodayDate()))
                        .toList();
            } else {
                tasks = tasks.stream()
                        .filter(task -> Objects.equals(task.getDate(), date))
                        .toList();
            }

            if (tasks.isEmpty()) {
                throw new EmplyTaskListException("No tasks found for the specified date.");
            }
        }
        return tasks;
    }

    /**
     * Creates a new task based on the given parameters.
     *
     * @param description the description of the task
     * @param date        the due date of the task
     * @param status      the status of the task
     * @return the created Task
     */
    private Task createTask(String description, String date, String status) {
        Task newTask = new Task(description);
        if (!Objects.equals(date, "no date")) {
            newTask.setDate(date);
        }
        if (!status.isEmpty()) {
            newTask.setStatus(TaskStatus.valueOf(status));
        }
        return newTask;
    }

    /**
     * Appends a task to the existing JSON array in the file.
     *
     * @param file    the file containing tasks
     * @param newTask the new task to append
     * @throws IOException if an error occurs during file writing
     */
    private void appendTaskToFile(File file, Task newTask) throws IOException {
        String content = new Scanner(file).useDelimiter("\\Z").next();
        String updatedContent = content.substring(0, content.length() - 1) + ",";

        try (FileWriter tempWriter = new FileWriter(file)) {
            tempWriter.write(updatedContent);
        }

        try (FileWriter fileWriter = new FileWriter(file, true);
             SequenceWriter sequenceWriter = mapper.writer().withDefaultPrettyPrinter().writeValues(fileWriter)) {
            sequenceWriter.write(newTask);
            fileWriter.write(']');
        }
    }
}
