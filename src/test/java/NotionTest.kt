import dev.scratch.remindbot.NotionHelper
import dev.scratch.remindbot.Task
import dev.scratch.remindbot.util.NotionTime
import notion.api.v1.NotionClient
import notion.api.v1.http.OkHttp4Client
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

//    @Test
//    fun taskTests() {
//        val client = NotionClient(System.getenv("notion"));
//        client.httpClient = OkHttp4Client()
//        val timeOffset: String = (ZoneOffset.systemDefault().rules.getOffset(Instant.now()).toString())
//        assertEquals(timeOffset, "-05:00")
//        val notionTest = NotionHelper(client)
//        val task = Task("Testing Task", NotionTime("2022-06-09T20:34:00.000-05:00"), "2022-06-09", false, true)
//        task.id = notionTest.addTask(task)
//        var tasks = notionTest.getTasks()
//        assertTrue(task in tasks)
//        task.id = notionTest.markAsCompleted(task.id)
//        tasks = notionTest.getTasks()
//        assertFalse(task in tasks)
//        task.id = notionTest.markAsNotStarted(task.id)
//        tasks = notionTest.getTasks()
//
//        val removeTask = tasks.filter { it.name == "Testing Task" }
//
//        notionTest.removeTask(removeTask[0].id)
//        tasks = notionTest.getTasks()
//
//        assertFalse(task in tasks)
//        println(LocalDate.now().toString())
//    }

}
