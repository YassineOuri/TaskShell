package com.example.TaskShell.exceptions;

public class EmplyTaskListException extends Exception{
    public EmplyTaskListException() {}

    // Constructor that accepts a message
    public EmplyTaskListException(String message)
    {
        super(message);
    }
}
