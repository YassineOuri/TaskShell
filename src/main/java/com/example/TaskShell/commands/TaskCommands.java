package com.example.TaskShell.commands;

import com.example.TaskShell.models.ANSIColors;
import com.example.TaskShell.models.Task;
import com.example.TaskShell.models.TaskStatus;
import com.example.TaskShell.services.TaskService;
import com.example.TaskShell.utils.DateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

/**
 * Command-line interface for managing tasks using Spring Shell.
 */
@ShellComponent
public class TaskCommands {

    private final String homeDir = System.getProperty("user.home", ".");
    private final File tasksFile = new File(homeDir, "tasks.json");
    private final TaskService taskService = new TaskService();




    /**
     * Initializes the task commands and ensures the tasks file exists.
     */
    public TaskCommands() {

        try {
            if (tasksFile.createNewFile()) {
                System.out.println(ANSIColors.greenText("[√]") + " Tasks file created");
            }
        } catch (IOException e) {
            System.out.println(ANSIColors.redText("An error occurred while accessing the tasks file"));
        } catch (SecurityException e) {
            System.out.println(ANSIColors.redText("Access denied when trying to write to file"));
        }
    }

    /**
     * Lists tasks based on various options.
     *
     * @param detailed Whether to display a detailed list of tasks.
     * @param table    Whether to display tasks in a table format.
     * @param date     Filter tasks by a specific date.
     * @param all      Whether to list all tasks.
     * @return A string representation of the tasks.
     */
    @ShellMethod(key = "list", value = "List tasks, if no argument is specified it lists today's tasks")
    public String listTasks(
            @ShellOption(value = {"--d", "--detailed"}, defaultValue = "false") Boolean detailed,
            @ShellOption(value = {"--t", "--table"}, defaultValue = "false") Boolean table,
            @ShellOption(defaultValue = "no date") String date,
            @ShellOption(value = {"--a", "--all"}, defaultValue = "false") Boolean all
    ) {
        return taskService.listTasks(all, detailed, table, date, tasksFile);
    }

    /**
     * Creates a new task with the specified description, date, and status.
     *
     * @param description The task description.
     * @param date        The task date (optional).
     * @param status      The task status (default: TODO).
     * @return A success message.
     */
    @ShellMethod(key = "add", value = "Create a task")
    public String createTask(
            String description,
            @ShellOption(value = {"--d", "--date"}, defaultValue = "no date") String date,
            @ShellOption(value = {"--s", "--status"}, defaultValue = "TODO") String status
    ) {
        taskService.addNewTask(tasksFile, description, date, status);
        return ANSIColors.greenText("[√] Task created successfully");
    }

    /**
     * Updates an existing task by its ID.
     *
     * @param taskID         The ID of the task to update.
     * @param newDescription The new description for the task.
     * @param date           The new date for the task (optional).
     * @return A success message or an error message if the task is not found.
     */
    @ShellMethod(key = "update", value = "Update a task by ID")
    public String modifyTask(
            String taskID,
            String newDescription,
            @ShellOption(value = "d", defaultValue = "no date") String date
    ) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Task> tasks = mapper.readValue(tasksFile, mapper.getTypeFactory().constructCollectionType(List.class, Task.class));

            Task taskToUpdate = tasks.stream()
                    .filter(task -> Objects.equals(task.getId().toString(), taskID))
                    .findFirst()
                    .orElse(null);

            if (taskToUpdate == null) {
                return "Task with ID " + taskID + " doesn't exist";
            }

            if (newDescription != null) {
                taskToUpdate.setDescription(newDescription);
            }
            if (!Objects.equals(date, "no date")) {
                taskToUpdate.setDate(date);
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(tasksFile, tasks);
            return "Task modified successfully";
        } catch (IOException e) {
            return "An error occurred while updating the task";
        }
    }

    /**
     * Marks a task as DONE.
     *
     * @param taskID The ID of the task to mark as DONE.
     * @return A success message or an error message if the task is not found.
     */
    @ShellMethod(key = "mark-done", value = "Mark a task by ID as DONE")
    public String markAsDone(String taskID) {
        return taskService.updateTaskStatus(tasksFile, taskID, TaskStatus.DONE);
    }

    /**
     * Marks a task as TODO.
     *
     * @param taskID The ID of the task to mark as TODO.
     * @return A success message or an error message if the task is not found.
     */
    @ShellMethod(key = "mark-todo", value = "Mark a task by ID as TODO")
    public String markAsTodo(String taskID) {
        return taskService.updateTaskStatus(tasksFile,taskID, TaskStatus.TODO);
    }

    /**
     * Deletes a task by its ID.
     *
     * @param taskID The ID of the task to delete.
     * @return A success message or an error message if the task is not found.
     */
    @ShellMethod(key = "delete", value = "Delete a task by ID")
    public String deleteTask(String taskID) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Task> tasks = mapper.readValue(tasksFile, mapper.getTypeFactory().constructCollectionType(List.class, Task.class));
            List<Task> updatedTasks = tasks.stream()
                    .filter(task -> !Objects.equals(task.getId().toString(), taskID))
                    .toList();

            mapper.writerWithDefaultPrettyPrinter().writeValue(tasksFile, updatedTasks);
            return "Task deleted successfully";
        } catch (IOException e) {
            return "An error occurred while deleting the task";
        }
    }

    /**
     * Moves tasks from one date to another.
     * @param from The source date. If no date is specified it defaults to the current date
     * @param to   The target date. If no date is specified it defaults to tomorrow
     * @return A success message or an error message if an error occurs.
     */
    @ShellMethod(key = "move-todo", value = "Moves Undone tasks from a date to a date")
    public String moveTodoTasks(
            @ShellOption(value = "from", defaultValue = "no date") String from,
            @ShellOption(value = "to", defaultValue = "no date") String to
    ) {
        String fromDate = !Objects.equals(from, "no date") ? from : DateUtils.getTodayDate();
        String toDate = !Objects.equals(to, "no date") ? to : DateUtils.getTomorrowDate();

        try {
            return ANSIColors.greenText(taskService.moveTodo(
                    tasksFile,
                    fromDate,
                    toDate)
            );
        } catch (DateTimeParseException e) {
            return ANSIColors.redText("Please specify a valid date with format day/month/year");
        }
        catch (IOException e) {
            return ANSIColors.redText("An error occurred while moving tasks");
        }
    }



}
