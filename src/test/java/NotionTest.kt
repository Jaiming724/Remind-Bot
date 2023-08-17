import dev.scratch.remindbot.NotionHelper
import dev.scratch.remindbot.Task
import dev.scratch.scheduler.util.DateTimeOptional
import dev.scratch.scheduler.util.SimpleDate
import dev.scratch.scheduler.util.SimpleDateTime
import notion.api.v1.NotionClient
import notion.api.v1.http.OkHttp4Client
import notion.api.v1.model.pages.PageProperty
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.time.*
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class NotionTest {

    @Test
    fun timeTest() {

//        val datetimeString = "2023-05-20T21:22:00.000-04:00"
//        val datetimeString2 = "2023-05-20T20:22:00.000-06:00"
//
//        val task = Task("testing", NotionTime(datetimeString2), datetimeString, false, false, "123")
//
//        //val datetimeString2 = "2023-05-20"
//
//        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'[HH:mm:ss.SSSXXX]")
//        val formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//
//        val dateTime = ZonedDateTime.parse(datetimeString, formatter)
//        val utcDateTime = dateTime.withZoneSameInstant(ZoneOffset.UTC)
//
//        //val dateTime2 = ZonedDateTime.parse(datetimeString2, formatter2)
//        println("Parsed datetime: ${utcDateTime.toLocalDateTime()}")
//        val a = LocalDateTime.now(ZoneOffset.UTC) > task.remindDate.getLocalDateTime();
//        println(a)
//        println(task.remindDate.getLocalDateTime())
        //  println("Parsed datetime: $dateTime2")
//        val utcDateTime: ZonedDateTime = dateTime.withZoneSameInstant(ZoneOffset.UTC)
//        val utcDateTime2: ZonedDateTime = dateTime2.withZoneSameInstant(ZoneOffset.UTC)
//        println(utcDateTime)
//        println(utcDateTime2)
    }

    @Test
    fun editTime() {
        val client = NotionClient(System.getenv("notion"))
        //create datetime format for 2023-08-20T13:00-04:00
        //val t = DateTimeOptional("2023-08-20T13:00:00.000-04:00")
        val formatter = DateTimeFormatter.ofPattern(DateTimeFormatter.ISO_OFFSET_DATE_TIME.toString())
        val dateTime = LocalDateTime.parse("2023-08-20T13:00:00.000-04:00", formatter)
//        when(t){
//            is SimpleDateTime -> {
//                println(t.dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
//            }
//
//            is SimpleDate -> {
//                println(t.date.toString())
//            }
//        }
//        val id = "d9c5615b-1bd4-4911-940b-d00a4e7c38a5"
//
//        val instant = Instant.parse("2023-08-11T15:20:00.000Z") // Parse the ISO string to Instant
//
//
//        val newYorkZone = ZoneId.of("America/New_York")
//        val newYorkOffset = newYorkZone.rules.getOffset(Instant.now())
//        val estDateTime = instant.atOffset(newYorkOffset) // Convert to OffsetDateTime with EST offset
//
//        val temp = client.updatePage(
//            pageId = id,
//            properties = mapOf(
//                "Remind_Date" to PageProperty(
//                    date = PageProperty.Date(estDateTime.toString())
//                ),
//            )
//        ).id

    }


    @Test
    fun taskTests() {
        val client = NotionClient(System.getenv("notion"));
        client.httpClient = OkHttp4Client()
        val timeOffset: String = (ZoneOffset.systemDefault().rules.getOffset(Instant.now()).toString())
        assertEquals(timeOffset, "-04:00")
        val notionTest = NotionHelper(client)
        val task = Task("Testing Task", DateTimeOptional("2023-07-30T20:34:00.000-04:00"), "2022-06-09", false, true)
        task.id = notionTest.addTask(task)
        var tasks = notionTest.getTasks()
        assertTrue(task in tasks)
        task.id = notionTest.markAsCompleted(task.id)
        tasks = notionTest.getTasks()
        assertFalse(task in tasks)
        task.id = notionTest.markAsNotStarted(task.id)
        tasks = notionTest.getTasks()

        val removeTask = tasks.filter { it.name == "Testing Task" }

        notionTest.removeTask(removeTask[0].id)
        tasks = notionTest.getTasks()

        assertFalse(task in tasks)
        println(LocalDate.now().toString())
    }

}
