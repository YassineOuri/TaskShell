package com.example.TaskShell.services;

import com.example.TaskShell.models.Task;
import com.example.TaskShell.exceptions.EmplyTaskListException;
import com.example.TaskShell.utils.DateUtils;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class ListService {




    public List<Task> listTasks(File file, Boolean all, String date) throws IOException, EmplyTaskListException {
        List<Task> tasks;
        ObjectMapper mapper = new ObjectMapper();

        tasks = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, Task.class));


        if(!all && Objects.equals(date, "no date")) {

            tasks = tasks.stream().filter(task -> Objects.equals(task.getDate(), DateUtils.getTodayDate())).toList();
            if (tasks.isEmpty()) {
                throw new EmplyTaskListException("No tasks are created for today");
            }
        }

        if (!Objects.equals(date, "no date") && !all) {
            tasks = tasks.stream().filter(task -> Objects.equals(task.getDate(), date)).toList();
            if (tasks.isEmpty()) {
                throw new EmplyTaskListException("No Tasks are created for this date");
            }
        }
        return tasks;
    }
}

