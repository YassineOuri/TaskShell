package com.example.TaskShell.config;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;
import org.springframework.context.event.EventListener;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class CustomPromptProvider implements PromptProvider {

    @Override
    public final AttributedString getPrompt() {

        return new AttributedString("Taskshell:>");

    }
}
