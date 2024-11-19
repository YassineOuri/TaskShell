package com.example.TaskShell.commands;

import com.example.TaskShell.exceptions.EmplyTaskListException;
import com.example.TaskShell.models.ANSIColors;
import com.example.TaskShell.models.Task;
import com.example.TaskShell.models.TaskStatus;
import com.example.TaskShell.services.ListService;
import com.example.TaskShell.utils.DateUtils;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.context.InteractionMode;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.*;
import java.nio.file.Paths;
import java.sql.Array;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@ShellComponent
public class TaskCommands {

    String homeDir = System.getProperty("user.home");
    File tasksFile = new File(homeDir + "/tasks.json");

    ListService listService = new ListService();


    public TaskCommands() throws IOException {
        tasksFile.createNewFile();
        /*System.out.println(" _________  ________  ________  ___  __    ________  ___       ___     \n" +
                "|\\___   ___\\\\   __  \\|\\   ____\\|\\  \\|\\  \\ |\\   ____\\|\\  \\     |\\  \\    \n" +
                "\\|___ \\  \\_\\ \\  \\|\\  \\ \\  \\___|\\ \\  \\/  /|\\ \\  \\___|\\ \\  \\    \\ \\  \\   \n" +
                "     \\ \\  \\ \\ \\   __  \\ \\_____  \\ \\   ___  \\ \\  \\    \\ \\  \\    \\ \\  \\  \n" +
                "      \\ \\  \\ \\ \\  \\ \\  \\|____|\\  \\ \\  \\\\ \\  \\ \\  \\____\\ \\  \\____\\ \\  \\ \n" +
                "       \\ \\__\\ \\ \\__\\ \\__\\____\\_\\  \\ \\__\\\\ \\__\\ \\_______\\ \\_______\\ \\__\\\n" +
                "        \\|__|  \\|__|\\|__|\\_________\\|__| \\|__|\\|_______|\\|_______|\\|__|\n" +
                "                        \\|_________|                                   ");*/
    }


    private String getTodayDate() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return LocalDate.now().format(dateTimeFormatter);
    }

    private String getTomorrowDate() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return LocalDate.now().plusDays(1).format(dateTimeFormatter);
    }

    private StringBuilder displaySimpleList(List<Task> tasks, String date) {
        StringBuilder output = new StringBuilder();
        output.append(
                ANSIColors.greenText("╔═══════════════════════════════════════════════════════════════╗\n" +
                "║                      Tasks Due : "+date+"                   ║\n" +
                "╚═══════════════════════════════════════════════════════════════╝\n"));
        for (Task task : tasks) {
            String checkString = task.getStatus() == TaskStatus.TODO ? ANSIColors.redText("[ ]") : ANSIColors.greenText("[x]");
            String taskOutput = checkString + " " + task.getDescription() + "\n";
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

    @ShellMethod(key = "list", value = "List tasks, if no argument is specified it lists today's tasks")
    public String listTasks(
            @ShellOption(defaultValue = "false", arity = 0, help = "Display a detailed list of tasks") Boolean d,
            @ShellOption(value = {"--t", "--table"}, defaultValue = "false", arity = 0, help = "Display a table of tasks") Boolean t,
            @ShellOption(defaultValue = "no date", help = "List tasks per a given date (required format dd/mm/yyyy)") String date,
            @ShellOption(value = {"--a", "--all"}, defaultValue = "false", help = "List all registred tasks") Boolean all


    ) {
        ObjectMapper mapper = new ObjectMapper();
        List<Task> tasks;
        try {
         tasks = listService.listTasks(tasksFile, all, date);
        } catch (IOException e) {
            e.printStackTrace();
            return ANSIColors.redText("An error occured while reading tasks, Try again later");
        } catch (EmplyTaskListException e) {
            return ANSIColors.redText(e.getMessage());
        }

        if (d) {
            return displayDetailedList(tasks).toString();
        }
        if (t) {
            return displayTabularList(tasks).toString();
        }
        String dueDate = !Objects.equals(date, "no date") ? date : DateUtils.getTodayDate();
        return displaySimpleList(tasks,dueDate).toString();

    }


    @ShellMethod(key = "add", value = "Create a task")
    public String createTask(
            String description,
            @ShellOption(value = {"--d", "--date"}, defaultValue = "no date", help = "Create a task for a given date") String date,
            @ShellOption(value = {"--s", "--status"},defaultValue = "TODO", help = "Create a task with given status") String status
    ) {
        File file = tasksFile;
        ObjectMapper mapper = new ObjectMapper();

        try (FileWriter fileWriter = new FileWriter(file, true)) {
            Task newTask = new Task(description);
            if(!Objects.equals(date, "no date")) {
                newTask.setDate(date);
            }

            if(!Objects.equals(status, "")) {
                newTask.setStatus(TaskStatus.valueOf(status));
            }

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

            return ANSIColors.greenText("✔ Task created successfully");
        } catch (IOException e) {
            e.printStackTrace();
            return ANSIColors.redText("An error occurred while creating your task! Try again later");
        }
    }

    @ShellMethod(key = "update" ,value="Update a task by ID")
    public String modifyTask(
            String taskID,
            String newDescription,
            @ShellOption(value = "d", defaultValue = "no date", help = "Create a task for a given date") String date
    ) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        List<Task> tasks;
        try {
            tasks = mapper.readValue(tasksFile, mapper.getTypeFactory().constructCollectionType(List.class, Task.class));
            if(!tasks.isEmpty()) {
                Task taskToUpdate = tasks.stream()
                        .filter(task -> Objects.equals(task.getId().toString(), taskID))
                        .findAny()
                        .orElse(null);
                if(taskToUpdate == null) {
                    return "Task of ID " + taskID + " doesn't exist";
                }

                if(newDescription != null) {
                    taskToUpdate.setDescription(newDescription);
                }

                if(!Objects.equals(date, "no date")) {
                    taskToUpdate.setDate(date);
                }

                mapper.writer().withDefaultPrettyPrinter().writeValue(tasksFile, tasks);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return "Task Modified Successfully";
    }


    @ShellMethod(key = "mark-done" ,  value = "Mark a task by ID as DONE")
    public String markAsDone(
            String taskID
    ) {
        ObjectMapper mapper = new ObjectMapper();

        List<Task> tasks;
        try {
            tasks = mapper.readValue(tasksFile, mapper.getTypeFactory().constructCollectionType(List.class, Task.class));
            if(!tasks.isEmpty()) {
                Task taskToUpdate = tasks.stream()
                        .filter(task -> Objects.equals(task.getId().toString(), taskID))
                        .findAny()
                        .orElse(null);
                if(taskToUpdate == null) {
                    return "Task of ID " + taskID + " doesn't exist";
                }

                taskToUpdate.setStatus(TaskStatus.DONE);

                mapper.writer().withDefaultPrettyPrinter().writeValue(tasksFile, tasks);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return "Task Modified Successfully";
    }


    @ShellMethod(key = "mark-todo" ,  value = "Mark a task by ID as TODO")
    public String markAsTodo(
            String taskID
    ) {
        ObjectMapper mapper = new ObjectMapper();

        List<Task> tasks;
        try {
            tasks = mapper.readValue(tasksFile, mapper.getTypeFactory().constructCollectionType(List.class, Task.class));
            if(!tasks.isEmpty()) {
                Task taskToUpdate = tasks.stream()
                        .filter(task -> Objects.equals(task.getId().toString(), taskID))
                        .findAny()
                        .orElse(null);
                if(taskToUpdate == null) {
                    return "Task of ID " + taskID + " doesn't exist";
                }

                taskToUpdate.setStatus(TaskStatus.TODO);

                mapper.writer().withDefaultPrettyPrinter().writeValue(tasksFile, tasks);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return "Task Modified Successfully";
    }

    @ShellMethod(key = "delete", value = "Delete a task by ID")
    public String deleteTask(
            String taskID
    ) {
        ObjectMapper mapper = new ObjectMapper();

        List<Task> tasks;
        try {
            tasks = mapper.readValue(tasksFile, mapper.getTypeFactory().constructCollectionType(List.class, Task.class));
            if(!tasks.isEmpty()) {
                List<Task> newTaskList = tasks.stream()
                        .filter(task -> !Objects.equals(task.getId().toString(), taskID))
                        .toList();


                mapper.writer().withDefaultPrettyPrinter().writeValue(tasksFile, newTaskList);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return "Task Deleted Successfully";
    }

    @ShellMethod(key = "move-todo", value = "Moves Undone tasks from a date to a date. If no dates are specified it moves tasks from today to tomorrow")
   public String moveTodoTasks(
           @ShellOption(value = "from" , defaultValue = "no date", help = "The date from which the tasks will be moved") String from,
           @ShellOption(value = "to" , defaultValue = "no date", help = "The date which the moved tasks will be placed") String to
    ) throws IOException {
        List<Task> tasks;

        ObjectMapper mapper = new ObjectMapper();
        String fromDate = !Objects.equals(from, "no date") ? from : getTodayDate();
        String toDate = !Objects.equals(to, "no date") ? to : getTomorrowDate();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Scanner scanner = new Scanner(System.in);
        System.out.printf("Move tasks from %s to %s ? (y/n)", fromDate, toDate);
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if(confirmation.equals("y") || confirmation.equals("yes")) {
            try {
                if(LocalDate.parse(fromDate, dateTimeFormatter).isAfter(LocalDate.parse(toDate, dateTimeFormatter))) {
                    return "'From' date should be earlier than 'To' date";
                }

                tasks = mapper.readValue(tasksFile, mapper.getTypeFactory().constructCollectionType(List.class, Task.class));
                List<Task> fromTasks = tasks.stream().filter(
                        task -> Objects.equals(task.getDate(), fromDate) && Objects.equals(task.getStatus(), TaskStatus.TODO)
                ).toList();

                // List<Task> clonedList = new ArrayList<Task>(fromTasks.size());
                for (Task tastToClone : fromTasks) {
                    Task clonedTask = tastToClone.clone();
                    clonedTask.setDate(toDate);
                    clonedTask.setNewId();
                    tasks.add(clonedTask);
                }
                mapper.writer().withDefaultPrettyPrinter().writeValue(tasksFile, tasks);


                return "Tasks moved successfully";
            } catch (DateTimeParseException e) {
                return "Invalid date format";
            } catch (IOException e) {
                return "An error occurred while accessing tasks file";
            }

        }

        else {
            return "Operation Cancelled";
        }



    }
}
