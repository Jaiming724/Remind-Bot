import dev.scratch.remindbot.NotionHelper
import dev.scratch.remindbot.Task
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NotionTest {
    @Test
    fun notionTest() {
        val timeOffset: String = (ZoneOffset.systemDefault().getRules().getOffset(Instant.now()).toString())
        assertEquals(timeOffset, "-04:00")
        val notionTest = NotionHelper()
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
