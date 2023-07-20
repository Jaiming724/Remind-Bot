import dev.scratch.remindbot.util.NotionTime
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

import java.time.format.DateTimeFormatter




class TimeTests {
    @Test
    fun notionTime() {
        val dateString = "2023-07-17T00:00:00.000-04:00"
        val n = NotionTime("2023-07-17T00:00:00.000-04:00")
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        // Parse the date string using the formatter

        // Parse the date string using the formatter
        val offsetDateTime = OffsetDateTime.parse(dateString, formatter)

        // Print the parsed OffsetDateTime object

        // Print the parsed OffsetDateTime object
        println("Parsed OffsetDateTime: $offsetDateTime")

        println(offsetDateTime)
        //println(n.getLocalDateTime())
    }
}