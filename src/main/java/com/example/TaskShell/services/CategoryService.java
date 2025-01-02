package com.example.TaskShell.services;

import com.example.TaskShell.models.ANSIColors;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CategoryService {
    public String createAndInsertCategory(
            File categoriesFile,
            String name
    ) throws IOException {

            BufferedWriter writer = new BufferedWriter(new FileWriter(categoriesFile, true));
            if(categoriesFile.length() == 0) {
                writer.write(name);
            }
            else {
                writer.newLine();
                writer.append(name);

            }
            writer.close();
            return ANSIColors.greenText("[√] Category created successfully");

    }

    public List<String> displayCatgeories(File file) throws IOException {
        List<String> categoryList = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        while(line != null) {
            categoryList.add(line);
            line = reader.readLine();
        }

        reader.close();
        return categoryList;

    }


    public boolean verifyCategory(File file, String category) throws IOException {
        List<String> categoryList = this.displayCatgeories(file);
        if(!categoryList.contains(category)) {
            Terminal terminal = TerminalBuilder.terminal();
            LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();
            System.out.println(ANSIColors.greenText(String.format("Category '%s' does not exist !", category)));
            while(true) {
                String input = lineReader.readLine("Do you want to create it (y/n) ").toLowerCase().trim();

                if(Arrays.asList("yes", "y").contains(input)) {
                    this.createAndInsertCategory(file, category);
                    break;
                }
                if(Arrays.asList("no", "n").contains(input)) {
                    return false;

                }
                else {
                    System.out.println(ANSIColors.redText("Invalid response, type y or n"));
                }
            }
            return true;


        } else return true;
    }
}
