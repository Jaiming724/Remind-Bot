import dev.scratch.remindbot.NotionHelper
import dev.scratch.remindbot.Task
import dev.scratch.remindbot.util.NotionTime
import notion.api.v1.NotionClient
import notion.api.v1.http.OkHttp4Client
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NotionTest {
    @Test
    fun taskTests() {
        val client = NotionClient(System.getenv("notion"));
        client.httpClient = OkHttp4Client()
        val timeOffset: String = (ZoneOffset.systemDefault().rules.getOffset(Instant.now()).toString())
        assertEquals(timeOffset, "-05:00")
        val notionTest = NotionHelper(client)
        val task = Task("Testing Task", NotionTime("2022-06-09T20:34:00.000-05:00"), "2022-06-09", false, true)
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
