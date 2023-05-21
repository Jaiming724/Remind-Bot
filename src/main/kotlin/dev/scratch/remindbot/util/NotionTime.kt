package dev.scratch.remindbot.util

import dev.scratch.remindbot.NotionHelper
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date

class NotionTime(val time: String) {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    fun getLocalDateTime(): LocalDateTime? {
        if (!hasTimeComponent(time)) {
            return null
        }

        val dateTime = ZonedDateTime.parse(time, dateTimeFormatter)
        val utcDateTime = dateTime.withZoneSameInstant(ZoneOffset.UTC)


        return utcDateTime.toLocalDateTime()
    }

    private fun hasTimeComponent(datetimeString: String): Boolean {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            val dateTime = LocalDateTime.parse(datetimeString, formatter)
            return true
        } catch (e: DateTimeParseException) {
            return false
        }


    }
}