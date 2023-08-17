import dev.scratch.remindbot.util.NotionTime
import org.junit.jupiter.api.Test
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters


class TimeTests {

    fun getNextTime(time: LocalTime, dayOfWeek: DayOfWeek): OffsetDateTime {
        var ld = LocalDate.now()
        ld = ld.with(TemporalAdjusters.next(dayOfWeek))
        val nextTime = LocalDateTime.of(ld, time)
        val zoneId = ZoneId.systemDefault()

        return nextTime.atZone(zoneId).toOffsetDateTime();
    }

    fun findMondayInBetween(startDate: LocalDate, endDate: LocalDate): LocalDate? {
        val mondayInBetween = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
        if (mondayInBetween.isAfter(endDate) || mondayInBetween.isEqual(endDate)) {
            return null; // No Monday found between the given date range
        }
        return mondayInBetween;
    }

    @Test
    fun dateWithRange() {
        val start = "8/8/2023"
        val end = "8/15/2023"
        val startDate = LocalDate.parse(start, DateTimeFormatter.ofPattern("M/d/yyyy"))
        val endDate = LocalDate.parse(end, DateTimeFormatter.ofPattern("M/d/yyyy"))
        val mondayInBetween: LocalDate? = findMondayInBetween(startDate, endDate)
        println(mondayInBetween)
    }

    @Test
    fun notionTime() {
        val dateString = "2023-07-17T03:00:00.000-04:00"
        val n = NotionTime("2023-07-17T00:00:00.000-04:00")
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        // Parse the date string using the formatter

        // Parse the date string using the formatter
        val offsetDateTime = OffsetDateTime.parse(dateString, formatter)

        // Print the parsed OffsetDateTime object

        // Print the parsed OffsetDateTime object
//        println("Parsed OffsetDateTime: $offsetDateTime")
//
//        println(offsetDateTime)
        println(getNextTime(LocalTime.now(), DayOfWeek.MONDAY))
        //println(n.getLocalDateTime())
    }
}