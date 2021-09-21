package dev.scratch.remindbot;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Bot {
    public static void main(String[] args) {


        DiscordApi api = new DiscordApiBuilder()
                .setToken("Nzc5NzYwMzU3Nzc4MzkxMTEw.X7lObA.psWZhO3z75jnohljrgRPXw6BrNs").setAllIntents()
                .login().join();
        Messenger messenger = new Messenger(api);
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        exec.scheduleAtFixedRate(() -> {
            try {
                messenger.getReminders();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);


    }



}
