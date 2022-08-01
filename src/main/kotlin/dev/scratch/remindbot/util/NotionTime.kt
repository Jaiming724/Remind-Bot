package dev.scratch.remindbot.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class NotionTime(val time: String) {
    fun getLocalDateTime(): LocalDateTime? {
        val timeOffset: String = (ZoneOffset.systemDefault().rules.getOffset(Instant.now()).toString())
        if (timeOffset !in time) {
            return null
        }
        return LocalDateTime.parse(this.time.replace(timeOffset, ""))
    }
}