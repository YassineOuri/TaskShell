package com.example.TaskShell.utils;

import com.example.TaskShell.models.ANSIColors;
import com.example.TaskShell.models.Task;
import com.example.TaskShell.models.TaskStatus;

import java.util.List;

public class TaskUtils {

    private static final int BOX_WIDTH = 57;

    /**
     * Prints a styled list header with the given title.
     *
     * @param title the title to display in the header
     */
    public static void printListHeader(String title) {
        String topBorder = createBoxBorder("┌", "─", "┐");
        String emptyLine = createBoxLine("");
        String textLine = createBoxLine(centerText(title));
        String bottomBorder = createBoxBorder("└", "─", "┘");

        // Print the header box
        System.out.println(topBorder);
        System.out.println(emptyLine);
        System.out.println(textLine);
        System.out.println(emptyLine);
        System.out.println(bottomBorder);
    }

    /**
     * Creates a formatted string builder for a simple list of tasks.
     *
     * @param tasks the list of tasks to display
     * @param date  the date for which tasks are displayed
     * @return a StringBuilder containing the formatted list
     */
    public static StringBuilder displaySimpleList(List<Task> tasks, String date) {
        StringBuilder output = new StringBuilder();
        printListHeader("Tasks Due " + date);

        for (Task task : tasks) {
            String taskCategory = task.getCategory() != null ? task.getCategory() : "Other";
            String checkString = task.getStatus() == TaskStatus.TODO
                    ? ANSIColors.redText("[ ]")
                    : ANSIColors.greenText("[x]");
            output.append(String.format(" %s %s: %s%n", checkString, ANSIColors.greenText(taskCategory), task.getDescription()));
        }

        return output;
    }

    /**
     * Creates a formatted string builder for a tabular list of tasks.
     *
     * @param tasks the list of tasks to display
     * @return a StringBuilder containing the formatted table
     */
    public static StringBuilder displayTabularList(List<Task> tasks) {
        StringBuilder output = new StringBuilder();
        String header = """
                ----------------------------------------------------
                |  ID  |      Task Description       | Status | Due Date  |
                ----------------------------------------------------
                """;
        output.append(header);

        for (Task task : tasks) {
            output.append(String.format(
                    "| %-4d | %-26s | %-6s | %-10s |%n",
                    task.getId(), task.getDescription(), task.getStatus(), task.getDate()
            ));
        }

        return output;
    }

    /**
     * Creates a detailed string builder for a list of tasks.
     *
     * @param tasks the list of tasks to display
     * @return a StringBuilder containing the detailed task information
     */
    public static StringBuilder displayDetailedList(List<Task> tasks) {
        StringBuilder output = new StringBuilder();

        for (Task task : tasks) {
            output.append(String.format("""
                    Task ID: %s
                    Description: %s
                    Status: %s
                    Date: %s
                    -------------------------------------------------
                    """, task.getId(), task.getDescription(), task.getStatus(), task.getDate()));
        }

        return output;
    }

    // Private utility methods

    /**
     * Creates a border for the header box.
     *
     * @param left  the left corner character
     * @param fill  the character to fill the border
     * @param right the right corner character
     * @return the formatted border string
     */
    private static String createBoxBorder(String left, String fill, String right) {
        return ANSIColors.greenText(left + fill.repeat(BOX_WIDTH - 2) + right);
    }

    /**
     * Creates a single line in the header box with centered text.
     *
     * @param content the text to center inside the box line
     * @return the formatted box line string
     */
    private static String createBoxLine(String content) {
        int totalPadding = BOX_WIDTH - content.length() - 2;
        int leftPadding = totalPadding / 2;
        int rightPadding = totalPadding - leftPadding;
        return ANSIColors.greenText("│" + " ".repeat(leftPadding) + content + " ".repeat(rightPadding) + "│");
    }

    /**
     * Centers the text for display in a box.
     *
     * @param text the text to center
     * @return the centered text
     */
    private static String centerText(String text) {
        return text.trim();
    }
}
