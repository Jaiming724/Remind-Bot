import dev.scratch.remindbot.NotionHelper
import dev.scratch.remindbot.util.NotionTime
import dev.scratch.scheduler.model.Schedule
import dev.scratch.scheduler.model.actions.HardAction
import dev.scratch.scheduler.service.Scheduler
import dev.scratch.scheduler.util.SimpleDateTime
import dev.scratch.scheduler.util.TimeFrame
import notion.api.v1.NotionClient
import notion.api.v1.model.databases.query.filter.PropertyFilter
import notion.api.v1.model.databases.query.filter.condition.SelectFilter
import notion.api.v1.model.databases.query.sort.QuerySort
import notion.api.v1.request.search.SearchRequest
import org.junit.jupiter.api.DisplayNameGenerator.Simple
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ScheduleTest {
    @Test
    fun getTodayTasks() {
        val client = NotionClient(System.getenv("notion"));
        val notionHelper = NotionHelper(client);

        for (result in notionHelper.getTodayTasks()) {
            print("testing")
            println(result.name)
            println(result.remindDate.toString())
        }
    }

    fun convertTo12Hours(militaryTime: String): String {

        val inputFormat = SimpleDateFormat("hh:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("hh:mm aa", Locale.getDefault())
        val date = inputFormat.parse(militaryTime)
        return outputFormat.format(date)
    }

    @Test
    fun scheduleTest() {
        val client = NotionClient(System.getenv("notion"));
        val notionHelper = NotionHelper(client);


        val scheduleService = Scheduler();

        for (res in notionHelper.getTodayTasks()) {
            when (val time = res.remindDate) {
                is SimpleDateTime -> {
                    val timeFrame = TimeFrame(time.dateTime.toLocalTime(), time.dateTime.toLocalTime().plusMinutes(60))
                    scheduleService.addAction(HardAction(res.name, timeFrame, arrayOf(LocalDate.now().dayOfWeek)))
                }

                else -> {}
            }
        }

        println("Course List")
        for (course in notionHelper.getClasses("Summer 2023")) {
            scheduleService.addAction(course)
            println("${course.content}: ${course.timeFrame}")
        }

        println("Tuesday Schedule")
        val map: Map<DayOfWeek, Schedule> = scheduleService.schedule()
        map[DayOfWeek.THURSDAY]?.schedule?.forEach {
            val hardAction = it.value as HardAction
            println("${it.value.content} ${hardAction.timeFrame}")
        }
    }


}