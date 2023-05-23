package dev.scratch.remindbot.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class NotionTime(val time: String) {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    fun getLocalDateTime(): LocalDateTime? {
        if (!hasTimeComponent(time)) {
            return null
        }

        val dateTime = ZonedDateTime.parse(time, dateTimeFormatter)
        val estTime = dateTime.withZoneSameInstant(ZoneId.of("America/New_York"))


        return estTime.toLocalDateTime()
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