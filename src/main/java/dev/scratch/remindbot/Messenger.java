package dev.scratch.remindbot;

import com.google.gson.Gson;
import com.vdurmont.emoji.EmojiParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Messenger {
    private CloseableHttpClient httpclient = HttpClients.createDefault();
    private HttpEntityEnclosingRequestBase httpPost;
    private Gson gson = new Gson();
    private DiscordApi api;
    private ScheduledExecutorService exec = Executors.newScheduledThreadPool(5);

    public Messenger(DiscordApi api) {
        this.api = api;
        httpPost = createRequestEntities("post", "https://api.notion.com/v1/databases/afe96bc15a6843a99e5311ffb524d41e/query", "{\n" +
                "  \"filter\": {\n" +
                "    \"or\": [\n" +
                "      {\n" +
                "        \"property\": \"Completed\",\n" +
                "        \"checkbox\": {\n" +
                "          \"equals\": false\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}");

    }

    public Task getReminders() throws IOException {
        CloseableHttpResponse response = null;
        Task task = null;
        try {
            response = httpclient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpEntity entity = response.getEntity();
        System.out.println(response.getStatusLine());

        if (entity != null && response.getStatusLine().getStatusCode() == 200) {
            String result = null;
            try {
                result = EntityUtils.toString(entity);
            } catch (IOException e) {
                e.printStackTrace();
            }
            task = gson.fromJson(result, Task.class);
            for (int i = 0; i < task.getResults().size(); i++) {
                String startTime = task.getResults().get(i).getProperties().getRemindDate().getDate().getStart();
                try {
                    LocalDateTime time = LocalDateTime.parse(startTime.replace("-04:00", ""));

                    if (LocalDateTime.now().compareTo(time) > 0 && (!task.getResults().get(i).getProperties().getReceived().isCheckbox() && !task.getResults().get(i).getProperties().getCompleted().isCheckbox())) {
                        editDueDate(task.getResults().get(i).getId(), LocalDateTime.now().plusMinutes(5).toString() + "-04:00");

                        handleEmbed(new Reminder(task.getResults().get(i).getProperties().getName().getTitle().get(0).getPlainText(), time),
                                task.getResults().get(i).getId());
                    }
                } catch (DateTimeParseException e) {
                }

            }
        }
        response.close();
        return task;
    }

    public void editDueDate(String id, String time) {
        HttpPatch patch = new HttpPatch("https://api.notion.com/v1/pages/" + id);
        patch.addHeader("Authorization", "Bearer secret_bUuKktfitbXYk3aObA7WT72sIkAXICBMznTqsGoj5Dn");
        patch.addHeader("Notion-Version", "2021-08-16");
        patch.addHeader("Content-Type", "application/json");
        HttpEntity stringEntity = new StringEntity("{\n" + "\"properties\": {\n" + "\"Remind_Date\":{     \"date\": {\n" + " \"start\":" + '"' + time + "\"," + "\n" + "\t\"end\":null\n" + "}}\n" + "}\n" + "}", ContentType.APPLICATION_JSON);
        patch.setEntity(stringEntity);
        try {
            CloseableHttpResponse res = httpclient.execute(patch);
            System.out.println(res.getStatusLine());
            res.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void markAsReceived(String id) {
        System.out.println("https://api.notion.com/v1/pages/" + id);
        HttpEntityEnclosingRequestBase patch;
        patch = createRequestEntities("patch", "https://api.notion.com/v1/pages/" + id, "{\n" +
                "  \"properties\": {\n" +
                "    \"Received\": { \"checkbox\": true }\n" +
                "  }\n" +
                "}");

        try {
            CloseableHttpResponse response = httpclient.execute(patch);
            System.out.println(response.getStatusLine());
            response.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void markAsCompleted(String id) {
        HttpEntityEnclosingRequestBase patch;
        System.out.println("https://api.notion.com/v1/pages/" + id);
        patch = createRequestEntities("patch", "https://api.notion.com/v1/pages/" + id, "{\n" +
                "  \"properties\": {\n" +
                "    \"Completed\": { \"checkbox\": true }\n" +
                "  }\n" +
                "}");

        try {
            CloseableHttpResponse response = httpclient.execute(patch);
            System.out.println(response.getStatusLine());
            response.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleEmbed(Reminder reminder, String checkboxID) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Reminder")
                .addField("Time", reminder.getLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")))
                .addField("Description", reminder.getContent())
                .setColor(Color.BLUE);
        api.getChannelById(794751364773314590L).flatMap(Channel::asServerTextChannel).ifPresent(serverTextChannel -> serverTextChannel.sendMessage("<@388155532130779156>"));
        api.getChannelById(794751364773314590L).flatMap(Channel::asServerTextChannel).ifPresent(serverTextChannel -> serverTextChannel.sendMessage(embed).thenAccept(message -> {
            message.addReaction("\uD83D\uDC4D");
            message.addReaction(EmojiParser.parseToUnicode(":white_check_mark:"));
            message.addReactionAddListener(event -> {
                if (event.getEmoji().equalsEmoji("\uD83D\uDC4D") && event.getUserId() != 779760357778391110L) {
                    exec.submit(() -> markAsReceived(checkboxID));
                }
                if (event.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":white_check_mark:")) && event.getUserId() != 779760357778391110L) {
                    exec.submit(() -> markAsCompleted(checkboxID));
                }
            }).removeAfter(5, TimeUnit.MINUTES);
        }));

    }

    private HttpEntityEnclosingRequestBase createRequestEntities(String type, String address, String jsonData) {
        HttpEntityEnclosingRequestBase request;
        if (type.equalsIgnoreCase("post")) {
            request = new HttpPost(address);
        } else {
            request = new HttpPatch(address);
        }
        request.addHeader("Authorization", "Bearer secret_bUuKktfitbXYk3aObA7WT72sIkAXICBMznTqsGoj5Dn");
        request.addHeader("Notion-Version", "2021-05-13");
        request.addHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(jsonData, ContentType.APPLICATION_JSON));
        return request;
    }

    public void sendSummary() {
        System.out.println("send summary is running");
        Task task = null;
        try {
            task = getReminders();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        if (task == null) {
            System.out.println("task was null");
            return;
        }
        List<Reminder> reminders = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        for (int i = 0; i < task.getResults().size(); i++) {
            String startTime;
            try {
                startTime = task.getResults().get(i).getProperties().getRemindDate().getDate().getStart();
            } catch (NullPointerException nullPointerException) {
                continue;
            }
            LocalDateTime time = LocalDateTime.parse(startTime.replace("-04:00", ""));
            if (dateFormatter.format(LocalDateTime.now()).equals(dateFormatter.format(time))) {
                reminders.add(new Reminder(task.getResults().get(i).getProperties().getName().getTitle().get(0).getPlainText(), time));
            }

        }
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(dateFormatter.format(LocalDateTime.now()) + " Summary")
                .setColor(Color.BLUE);

        for (Reminder reminder : reminders) {
            embed.addField(timeFormatter.format(reminder.getLocalDateTime()), reminder.getContent());
        }
        api.getChannelById(794751364773314590L).flatMap(Channel::asServerTextChannel).ifPresent(serverTextChannel -> serverTextChannel.sendMessage(embed));


    }
}
