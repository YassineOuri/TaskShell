package com.example.TaskShell.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public final class FileUtils {

    public static void emptyFile(File file) throws IOException {

        FileWriter fwOb = new FileWriter(file, false);
        PrintWriter pwOb = new PrintWriter(fwOb, false);
        pwOb.flush();
        pwOb.close();
        fwOb.close();
    }
}
