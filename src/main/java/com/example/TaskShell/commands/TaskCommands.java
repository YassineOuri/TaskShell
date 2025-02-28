package com.example.TaskShell.commands;

import com.example.TaskShell.models.ANSIColors;
import com.example.TaskShell.utils.TaskUtils;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import com.example.TaskShell.models.Category;
import com.example.TaskShell.models.Task;
import com.example.TaskShell.models.TaskStatus;
import com.example.TaskShell.services.CategoryService;
import com.example.TaskShell.services.TaskService;
import com.example.TaskShell.utils.DateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
    private final File tasksFile = new File(homeDir + "/TaskShell/tasks.json");
    private final File categoriesFile = new File(homeDir + "/TaskShell/categories.txt");


    private final TaskService taskService = new TaskService();
    private final CategoryService categoryService = new CategoryService();

    private void initializeStorageDirectory() {
        File theDir = new File("/" + homeDir + "/TaskShell");
        if (!theDir.exists()) {
            theDir.mkdirs();

        }


    }




    /**
     * Initializes the task commands and ensures the tasks file exists.
     */
    public TaskCommands() {


        try {

            initializeStorageDirectory();
            tasksFile.createNewFile();
            categoriesFile.createNewFile();

            System.out.println(ANSIColors.greenText("[√]") + " Files are ready");

        } catch (IOException e) {
            System.out.println(ANSIColors.redText("An error occurred while accessing the tasks file" + e.getMessage()));
        } catch (SecurityException e) {
            System.out.println(ANSIColors.redText("Access denied when trying to write to file"));
        }

        System.out.println(" _____         _     ____  _          _ _ \n" +
                "|_   _|_ _ ___| | __/ ___|| |__   ___| | |\n" +
                "  | |/ _` / __| |/ /\\___ \\| '_ \\ / _ \\ | |\n" +
                "  | | (_| \\__ \\   <  ___) | | | |  __/ | |\n" +
                "  |_|\\__,_|___/_|\\_\\|____/|_| |_|\\___|_|_|\n" +
                "\n");
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
            @ShellOption(value = {"--tab", "--table"}, defaultValue = "false") Boolean table,
            @ShellOption(defaultValue = "no date") String date,
            @ShellOption(value = "--t", defaultValue = "false") boolean tomorrow,
            @ShellOption(value = {"--a", "--all"}, defaultValue = "false") Boolean all
    ) {
        return taskService.listTasks(all, detailed, table, tomorrow, date, tasksFile);
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
            @ShellOption(value = {"--s", "--status"}, defaultValue = "TODO") String status,
            @ShellOption(value = "--t", defaultValue = "false") boolean tomorrow,
            @ShellOption(value = {"--c", "--category"}, help = "Category associated with the task") String category
    )
    {
        taskService.addNewTask(tasksFile, categoriesFile, description, date, status, tomorrow, category);
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
        return taskService.updateTaskStatus(tasksFile, taskID, TaskStatus.TODO);
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
     *
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
        } catch (IOException e) {
            return ANSIColors.redText("An error occurred while moving tasks");
        }
    }

    /**
     * Creates and inserts a new category
     *
     * @param name The name of the category
     * @return A success message or an error message if an error occurs.
     */
    @ShellMethod(key = "category add", value = "Creates a new category")
    public String createCategory(
            String name
    ) {
        try {
            return categoryService.createAndInsertCategory(categoriesFile, name);
        } catch (IOException e) {
            return ANSIColors.redText("An error occurred while creating a new category !");
        }
    }

    @ShellMethod(key = "category list", value = "Displays all categories")
    public void displayCategories(
    ) throws IOException {
        int pageSize = 5;
        int currentPage = 0;
        Terminal terminal = TerminalBuilder.terminal();
        LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();

        try {
            List<String> categoryList =  categoryService.displayCategories(categoriesFile);
            if(categoryList.isEmpty()) {
                System.out.println(ANSIColors.greenText("No categories are created yet ! \n" +
                        "Create a new category using `category add`"));
                return;
            }

            while (true) {
                terminal.puts(InfoCmp.Capability.clear_screen);
                int start = currentPage * pageSize;
                int end = Math.min(start + pageSize, categoryList.size());

                TaskUtils.printListHeader(String.format("Categories %d -> %d: ", start, end));
                for (int i = start; i < end; i++) {
                    System.out.println((i + 1) + ". " + categoryList.get(i));
                }

                System.out.println("\n[Press 'n' for next, 'p' for previous, 'q' to quit]");
                String input = lineReader.readLine("Enter your choice: ").trim();
                if ("n".equals(input) && end < categoryList.size()) {
                    currentPage++;
                } else if ("p".equals(input) && currentPage > 0) {
                    currentPage--;
                } else if ("q".equals(input)) {
                    break;
                }

            }
        } catch (IOException e) {
            System.out.println(ANSIColors.redText("An error occurred while creating a new category !"));
        }
    }


    @ShellMethod(key = "update-category", value = "Update the category of a given task ID")
    public String updateCategory(
            String taskID,
            String newCategory
    )  {

        try {

            if(newCategory == null) {
                return ANSIColors.redText("Please specify a category");

            }

            boolean categoryVerified = categoryService.verifyCategory(categoriesFile, newCategory);

            if(!categoryVerified) {
                return ANSIColors.redText("Aborted");
            }

            return taskService.updateTaskCategory(tasksFile, taskID, newCategory);

        } catch(IOException e) {
            return ANSIColors.redText("An error occurred while updating category!");
        }
    }

    @ShellMethod(key = "category delete", value = "Deletes a category")
    public String deleteCategory(
            String category
    )  {
        try {
            boolean categoryDeleted = categoryService.deleteCategory(categoriesFile, category);
            if(categoryDeleted) {
                return ANSIColors.greenText("Category deleted successfully!");
            }

            return "";

        } catch(IOException e) {
            return ANSIColors.redText("An error occurred while deleting category!");
        }
    }




}
