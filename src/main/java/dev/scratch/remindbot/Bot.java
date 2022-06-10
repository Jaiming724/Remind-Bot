package dev.scratch.remindbot;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Bot {
    public static void main(String[] args) {


        DiscordApi api = new DiscordApiBuilder()
                .setToken(System.getenv("token")).setAllIntents()
                .login().join();

        ReminderChecker reminderChecker = new ReminderChecker(api);
        api.addMessageCreateListener(event -> {
            if (event.getMessageContent().equalsIgnoreCase("!summary")) {
                reminderChecker.sendSummary();
            }
        });
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        exec.scheduleAtFixedRate(() -> {
            try {
                reminderChecker.checkReminders();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);

    }


}
