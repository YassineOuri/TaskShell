package com.example.TaskShell.services;

import com.example.TaskShell.exceptions.EmptyTaskListException;
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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

@Service
public class TaskService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final CategoryService categoryService = new CategoryService();

    /**
     * Lists tasks based on the given parameters and file contents.
     *
     * @param displayAll whether to display all tasks
     * @param isDetailed whether to display tasks in detailed format
     * @param isTable    whether to display tasks in tabular format
     * @param date       the date to filter tasks
     * @param file       the file containing tasks
     * @return a formatted string representation of tasks
     */
    public String listTasks(boolean displayAll, boolean isDetailed, boolean isTable, boolean tomorrow, String date, File file) {
        try {

            if(file.length() == 0) {
                return ANSIColors.redText("There are no tasks registered yet ! \n" +
                        "Add new tasks using add command");
            }

            List<Task> tasks = readTasksFromFile(file);



            // Filter tasks based on date conditions
            tasks = filterTasks(tasks, displayAll, date, tomorrow);

            // Format tasks based on the desired output style
            if (isDetailed) {
                return TaskUtils.displayDetailedList(tasks).toString();
            } else if (isTable) {
                return TaskUtils.displayTabularList(tasks).toString();
            } else {
                String dueDate = tomorrow ? DateUtils.getTomorrowDate() :
                        (Objects.equals(date, "no date") ? DateUtils.getTodayDate() : date);
                return TaskUtils.displaySimpleList(tasks, dueDate).toString();
            }
        } catch (EmptyTaskListException e) {
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
    public void addNewTask(File file, File categoriesFile, String description, String date, String status, boolean tomorrow, String category) {
        try (FileWriter fileWriter = new FileWriter(file, true)) {

            Task newTask = createTask(description, date, status, tomorrow);

            if (category != null && categoryService.verifyCategory(categoriesFile, category)) {
                newTask.setCategory(category);
            }



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
     * @throws EmptyTaskListException if no tasks match the filter conditions
     */
    private List<Task> filterTasks(List<Task> tasks, boolean displayAll, String date, boolean tomorrow) throws EmptyTaskListException {
        if (!displayAll) {

            if(tomorrow) {
                tasks = tasks.stream()
                        .filter(task -> Objects.equals(task.getDate(), DateUtils.getTomorrowDate()))
                        .toList();
            }
            else {
                switch (date) {
                    case "no date" -> {
                        {
                            tasks = tasks.stream()
                                    .filter(task -> Objects.equals(task.getDate(), DateUtils.getTodayDate()))
                                    .toList();
                        }

                    }
                    case "tomorrow" -> {
                        {
                            tasks = tasks.stream()
                                    .filter(task -> Objects.equals(task.getDate(), DateUtils.getTomorrowDate()))
                                    .toList();
                        }

                    }
                    default -> {
                        {
                            tasks = tasks.stream()
                                    .filter(task -> Objects.equals(task.getDate(), date))
                                    .toList();
                        }

                    }
                }
            }

            if (tasks.isEmpty()) {
                throw new EmptyTaskListException("No tasks found for the specified date.");
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
    private Task createTask(String description, String date, String status, boolean tomorrow) {
        Task newTask = new Task(description);
        String taskDate = date;

        if(tomorrow || Objects.equals(date, "tomorrow")) {
            taskDate = DateUtils.getTomorrowDate();
        } else if (Objects.equals(date, "no date")) {
            taskDate = DateUtils.getTodayDate();
        }



        newTask.setDate(taskDate);


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



    /**
     * Updates the status of a task.
     *
     * @param taskID The ID of the task.
     * @param status The new status.
     * @return A success message or an error message if the task is not found.
     */
    public String updateTaskStatus(File file, String taskID, TaskStatus status) {
        try {
            List<Task> tasks = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, Task.class));
            Task taskToUpdate = this.getTaskByID(tasks, taskID);

            if (taskToUpdate == null) {
                return "Task with ID " + taskID + " doesn't exist";
            }

            taskToUpdate.setStatus(status);
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, tasks);
            return "Task status updated successfully";
        } catch (IOException e) {
            return "An error occurred while updating the task status";
        }
    }



    public String updateTaskCategory(File file, String taskID, String category) {
        try {
            List<Task> tasks = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, Task.class));
            Task taskToUpdate = this.getTaskByID(tasks, taskID);

            if (taskToUpdate == null) {
                return "Task with ID " + taskID + " doesn't exist";
            }

            taskToUpdate.setCategory(category);
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, tasks);
            return ANSIColors.greenText("Task category updated successfully");
        } catch (IOException e) {
            return ANSIColors.redText("An error occurred while updating the task status");
        }
    }

    public String moveTodo(File file, String from, String to) throws IOException, DateTimeParseException {

        Scanner scanner = new Scanner(System.in);
        System.out.printf("Do you want to move undone tasks from %s to %s? (y/n): ", from, to);
        String response = scanner.nextLine().toLowerCase().trim();
        if(!Arrays.asList("y", "yes", "Yes").contains(response)) {
            return "Aborted";
        }

         if (LocalDate.parse(from, DateUtils.dateTimeFormatter).isAfter(LocalDate.parse(to, DateUtils.dateTimeFormatter))) {
                return "'From' date should be earlier than 'To' date";
            }

            ObjectMapper mapper = new ObjectMapper();
            List<Task> tasks = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, Task.class));
            List<Task> fromTasks = tasks.stream()
                    .filter(task -> Objects.equals(task.getDate(), from) && task.getStatus() == TaskStatus.TODO)
                    .toList();

            for (Task task : fromTasks) {
                Task clonedTask = task.clone();
                clonedTask.setDate(to);
                clonedTask.setNewId();
                tasks.add(clonedTask);
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, tasks);
            return "Tasks moved successfully";

    }


    public Task getTaskByID(List<Task> tasks, String taskID) throws IOException {
        return  tasks.stream()
                .filter(task -> Objects.equals(task.getId().toString(), taskID))
                .findFirst()
                .orElse(null);
    }

}
