package com.example.TaskShell.exceptions;

public class EmptyTaskListException extends Exception{

    // Constructor that accepts a message
    public EmptyTaskListException(String message)
    {
        super(message);
    }
}
