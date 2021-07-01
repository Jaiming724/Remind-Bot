package dev.scratch.remindbot;

import java.time.LocalDateTime;

public class Reminder {
    private String content;
    private transient LocalDateTime localDateTime;

    public Reminder(String message, LocalDateTime localDateTime) {
        content = message;
        this.localDateTime = localDateTime;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }
}
