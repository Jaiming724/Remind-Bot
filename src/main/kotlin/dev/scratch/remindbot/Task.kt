package dev.scratch.remindbot

import dev.scratch.remindbot.util.NotionTime
import dev.scratch.scheduler.util.DateTimeOptional
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class Task(
    val name: String,
    val remindDate: DateTimeOptional,
    val dueDate: String,
    val completed: Boolean,
    val received: Boolean,
    var id: String = "UNKNOWN"


) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Task

        return id == other.id
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + remindDate.hashCode()
        result = 31 * result + dueDate.hashCode()
        result = 31 * result + completed.hashCode()
        result = 31 * result + received.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }
}
