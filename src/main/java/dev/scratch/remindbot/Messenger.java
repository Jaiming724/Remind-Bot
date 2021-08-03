package dev.scratch.remindbot;

import com.google.gson.Gson;
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
import java.awt.Color;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Messenger {
    private CloseableHttpClient httpclient = HttpClients.createDefault();
    private HttpEntityEnclosingRequestBase httpPost;
    private Gson gson = new Gson();
    private DiscordApi api;

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

    public Task getReminders() {
        CloseableHttpResponse response = null;
        Task task = null;
        try {
            response = httpclient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpEntity entity = response.getEntity();
        if (entity != null && response.getStatusLine().getStatusCode() == 200) {
            String result = null;
            try {
                result = EntityUtils.toString(entity);
            } catch (IOException e) {
                e.printStackTrace();
            }
            task = gson.fromJson(result, Task.class);
        }
        try {
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return task;


    }

    public void sendReminders() {
        Task task = getReminders();
        if (task == null) {
            return;
        }
        for (int i = 0; i < task.getResults().size(); i++) {
            String startTime;
            try {
                startTime = task.getResults().get(i).getProperties().getDueDate().getDate().getStart();
            } catch (NullPointerException nullPointerException) {
                continue;
            }
            LocalDateTime time = LocalDateTime.parse(startTime.replace("-04:00", ""));
            if (LocalDateTime.now().compareTo(time) > 0 && (!task.getResults().get(i).getProperties().getReceived().isCheckbox())) {
                sendReminderEmbed(new Reminder(task.getResults().get(i).getProperties().getName().getTitle().get(0).getPlainText(), time),
                        task.getResults().get(i).getId());
            }

        }
    }

    public void markAsReceived(String id) {
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
            //test
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public void sendReminderEmbed(Reminder reminder, String checkboxID) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Reminder")
                .addField("Time", reminder.getLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")))
                .addField("Description", reminder.getContent())
                .setColor(Color.BLUE);
        api.getChannelById(794751364773314590L).flatMap(Channel::asServerTextChannel).ifPresent(serverTextChannel -> serverTextChannel.sendMessage("<@388155532130779156>"));
        api.getChannelById(794751364773314590L).flatMap(Channel::asServerTextChannel).ifPresent(serverTextChannel -> serverTextChannel.sendMessage(embed));
        markAsReceived(checkboxID);

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
        Task task = getReminders();
        if (task == null) {
            return;
        }
        List<Reminder> reminders = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        for (int i = 0; i < task.getResults().size(); i++) {
            String startTime;
            try {
                startTime = task.getResults().get(i).getProperties().getDueDate().getDate().getStart();
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
