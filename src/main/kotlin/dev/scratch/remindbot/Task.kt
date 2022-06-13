package dev.scratch.remindbot

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

data class Task(
    val name: String,
    val remindDate: String,
    val dueDate: String,
    val completed: Boolean,
    val received: Boolean,
    var id: String = "UNKNOWN"

) {
    fun getLocalDateTime(): LocalDateTime? {
        val timeOffset: String = (ZoneOffset.systemDefault().rules.getOffset(Instant.now()).toString())
        if (timeOffset !in remindDate) {
            return null
        }
        return LocalDateTime.parse(this.remindDate.replace(timeOffset, ""))
    }
}
