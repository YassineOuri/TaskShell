package com.example.TaskShell.services;

import com.example.TaskShell.models.ANSIColors;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
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

}
