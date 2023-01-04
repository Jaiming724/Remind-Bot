import dev.scratch.remindbot.util.NotionTime
import dev.scratch.scheduler.model.actions.HardAction
import dev.scratch.scheduler.service.ScheduleService
import dev.scratch.scheduler.util.TimeFrame
import notion.api.v1.NotionClient
import notion.api.v1.model.databases.query.filter.PropertyFilter
import notion.api.v1.model.databases.query.filter.condition.SelectFilter
import notion.api.v1.model.databases.query.sort.QuerySort
import notion.api.v1.request.search.SearchRequest
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.util.*


class ScheduleTest {
    @Test
    fun scheduleTest() {
        val client = NotionClient(System.getenv("notion"));
        val database = client.search(
            query = "Classes",
            filter = SearchRequest.SearchFilter("database", property = "object")
        ).results.find { it.asDatabase().properties.containsKey("Name") }?.asDatabase()
            ?: throw IllegalStateException("Could not find database")
        val queryResult =
            client.queryDatabase(
                databaseId = database.id,
                filter = PropertyFilter(
                    property = "Duration",
                    select = SelectFilter(equals = "Spring 2023")
                ),
                sorts =
                listOf(
                    QuerySort(property = "title"),
                )
            )
        val courses: MutableList<HardAction> = mutableListOf()

        for (result in queryResult.results) {
            val map = result.properties
            val content: String? = map["Name"]?.title?.get(0)?.plainText
            val start = NotionTime(map["Time"]?.date?.start!!)
            val end = NotionTime(map["Time"]?.date?.end!!)
            val mutableList: MutableList<DayOfWeek> = mutableListOf()
            for (i in map["Days"]?.multiSelect!!) {
                mutableList.add(getDayOfWeek(i.name!!))
            }
            val timeFrame = TimeFrame(start.getLocalDateTime()?.toLocalTime(), end.getLocalDateTime()?.toLocalTime())
            courses.add(HardAction(content, timeFrame, mutableList.toTypedArray()))
        }
        val scheduleService = ScheduleService();
        println("Course List")
        for (course in courses) {
            scheduleService.addAction(course)
            println("${course.content}: ${course.timeFrame}")
        }
        println("Monday Schedule")
        scheduleService.schedule()
        scheduleService.getSchedule(DayOfWeek.MONDAY).printSchedule()
    }

    private fun getDayOfWeek(day: String): DayOfWeek {
        return when (day.lowercase(Locale.getDefault())) {
            "monday" -> DayOfWeek.MONDAY
            "tuesday" -> DayOfWeek.TUESDAY
            "wednesday" -> DayOfWeek.WEDNESDAY
            "thursday" -> DayOfWeek.THURSDAY
            "friday" -> DayOfWeek.FRIDAY
            "saturday" -> DayOfWeek.SATURDAY
            "sunday" -> DayOfWeek.SUNDAY
            else -> throw IllegalArgumentException()
        }
    }
}