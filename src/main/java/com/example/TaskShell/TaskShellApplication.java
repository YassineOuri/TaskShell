package com.example.TaskShell;

import com.example.TaskShell.commands.TaskCommands;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.EnableCommand;

@SpringBootApplication
public class TaskShellApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskShellApplication.class, args);

	}

}
