package dev.scratch.remindbot;

import notion.api.v1.NotionClient;
import notion.api.v1.http.OkHttp4Client;
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
        NotionClient client = new NotionClient(System.getenv("notion"));
        client.setHttpClient(new OkHttp4Client());
        ReminderChecker reminderChecker = new ReminderChecker(api, client);
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
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 0, 1, TimeUnit.SECONDS);

    }


}
