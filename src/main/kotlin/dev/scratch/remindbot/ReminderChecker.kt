package dev.scratch.remindbot

import com.vdurmont.emoji.EmojiParser
import notion.api.v1.NotionClient
import notion.api.v1.http.OkHttp4Client
import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.Channel
import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.message.reaction.ReactionAddEvent
import java.awt.Color
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ReminderChecker(private val api: DiscordApi, client: NotionClient) {

    private val notionHelper = NotionHelper(client);
    private val exec = Executors.newScheduledThreadPool(5)

    fun checkReminders() {
        val tasks = notionHelper.getTasks()
        for (task in tasks) {
            if (task.remindDate.getLocalDateTime() != null && LocalDateTime.now(ZoneOffset.UTC) > task.remindDate.getLocalDateTime() && (!task.received && task.dueDate != "UNKNOWN")) {
                sendEmbed(task)
                notionHelper.updateTaskRemindDate(task.id)
            }
        }
    }

    fun sendSummary() {
        val tasks = notionHelper.getTasks()
        val embed = EmbedBuilder()
            .setTitle(LocalDate.now().toString())

        for (task in tasks) {
            if (LocalDate.now() == task.remindDate.getLocalDateTime()?.toLocalDate()) {
                embed.addField(
                    "Time",
                    task.remindDate.getLocalDateTime()?.format(DateTimeFormatter.ofPattern("hh:mm a"))
                )
                    .addField("Description", task.name)
            }
        }
        api.getChannelById(794751364773314590L).flatMap { obj: Channel -> obj.asServerTextChannel() }
            .ifPresent { serverTextChannel: ServerTextChannel ->
                serverTextChannel.sendMessage(
                    "<@388155532130779156>"
                )
                serverTextChannel.sendMessage(embed)
            }
    }

    private fun sendEmbed(task: Task) {
        val embed = EmbedBuilder()
            .setTitle("Reminder")
            .addField(
                "Time",
                task.remindDate.getLocalDateTime()?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"))
            )
            .addField("Description", task.name)
            .setColor(Color.BLUE)
        api.getChannelById(794751364773314590L).flatMap { obj: Channel -> obj.asServerTextChannel() }
            .ifPresent { serverTextChannel: ServerTextChannel ->
                serverTextChannel.sendMessage(
                    "<@388155532130779156>"
                )
            }
        api.getChannelById(794751364773314590L).flatMap { obj: Channel -> obj.asServerTextChannel() }
            .ifPresent { serverTextChannel: ServerTextChannel ->
                serverTextChannel.sendMessage(embed).thenAccept { message: Message ->
                    message.addReaction("\uD83D\uDC4D")
                    message.addReaction(EmojiParser.parseToUnicode(":white_check_mark:"))
                    message.addReactionAddListener { event: ReactionAddEvent ->
                        if (event.emoji
                                .equalsEmoji("\uD83D\uDC4D") && event.userId != 779760357778391110L
                        ) {
                            exec.submit { notionHelper.markAsReceived(task.id) }
                        }
                        if (event.emoji
                                .equalsEmoji(EmojiParser.parseToUnicode(":white_check_mark:")) && event.userId != 779760357778391110L
                        ) {
                            exec.submit { notionHelper.markAsCompleted(task.id) }
                        }
                    }.removeAfter(5, TimeUnit.MINUTES)
                }
            }
    }
}