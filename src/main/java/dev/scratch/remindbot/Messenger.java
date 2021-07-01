package dev.scratch.remindbot;

import com.google.gson.Gson;
import com.vdurmont.emoji.EmojiParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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

    public void getReminders() throws IOException {
        CloseableHttpResponse response = null;
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
            Task task = gson.fromJson(result, Task.class);
            for (int i = 0; i < task.getResults().size(); i++) {
                String startTime = task.getResults().get(i).getProperties().getDueDate().getDate().getStart();
                LocalDateTime time = LocalDateTime.parse(startTime.replace("-04:00", ""));
                if (LocalDateTime.now().compareTo(time) > 0 && (!task.getResults().get(i).getProperties().getReceived().isCheckbox() || !task.getResults().get(i).getProperties().getCompleted().isCheckbox())) {
                    editDueDate(task.getResults().get(i).getId(), time.plusMinutes(5).toString() + "-04:00");

                    handleEmbed(new Reminder(task.getResults().get(i).getProperties().getName().getTitle().get(0).getPlainText(), time),
                            task.getResults().get(i).getId());

                }

            }
        }
        response.close();
    }

    public void editDueDate(String id, String time) {
        HttpPatch patch = new HttpPatch("https://api.notion.com/v1/pages/" + id);
        patch.addHeader("Authorization", "Bearer secret_bUuKktfitbXYk3aObA7WT72sIkAXICBMznTqsGoj5Dn");
        patch.addHeader("Notion-Version", "2021-05-13");
        patch.addHeader("Content-Type", "application/json");
        HttpEntity stringEntity = new StringEntity("{\n" + "\"properties\": {\n" + "\"Due_Date\":{     \"date\": {\n" + " \"start\":" + '"' + time + "\"," + "\n" + "\t\"end\":null\n" + "}}\n" + "}\n" + "}", ContentType.APPLICATION_JSON);
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
            HttpResponse response = httpclient.execute(patch);
            System.out.println(response.getStatusLine());

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
            HttpResponse response = httpclient.execute(patch);
            System.out.println(response.getStatusLine());

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
                    markAsReceived(checkboxID);
                }
                if (event.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":white_check_mark:")) && event.getUserId() != 779760357778391110L) {
                    markAsCompleted(checkboxID);
                }
            });
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
}
