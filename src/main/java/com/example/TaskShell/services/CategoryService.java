package com.example.TaskShell.services;

import com.example.TaskShell.models.ANSIColors;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
}
