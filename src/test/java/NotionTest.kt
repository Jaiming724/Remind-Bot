import dev.scratch.remindbot.NotionHelper
import dev.scratch.remindbot.Task
import notion.api.v1.NotionClient
import notion.api.v1.http.OkHttp4Client
import notion.api.v1.logging.StdoutLogger
import notion.api.v1.model.blocks.Block
import notion.api.v1.model.blocks.BlockType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneOffset
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.log
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NotionTest {
    @Test
    fun notionTest() {
        val client = NotionClient(System.getenv("notion"));
        client.httpClient = OkHttp4Client()

        val timeOffset: String = (ZoneOffset.systemDefault().rules.getOffset(Instant.now()).toString())
        assertEquals(timeOffset, "-04:00")
        val notionTest = NotionHelper(client)
        val task = Task("Testing Task", "2022-06-09T20:34:00.000-04:00", "2022-06-09", false, true)
        task.id = notionTest.addTask(task)
        var tasks = notionTest.getTasks()
        assertTrue(task in tasks)
        val removeTask = tasks.filter { it.name == "Testing Task" }

        notionTest.removeTask(removeTask[0].id)
        tasks = notionTest.getTasks()

        assertFalse(task in tasks)
    }


}
