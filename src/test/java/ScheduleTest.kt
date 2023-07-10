import dev.scratch.remindbot.NotionHelper
import dev.scratch.remindbot.util.NotionTime
import dev.scratch.scheduler.model.actions.HardAction
import dev.scratch.scheduler.service.ScheduleService
import dev.scratch.scheduler.util.TimeFrame
import notion.api.v1.NotionClient
import notion.api.v1.model.databases.query.filter.CompoundFilter
import notion.api.v1.model.databases.query.filter.PropertyFilter
import notion.api.v1.model.databases.query.filter.condition.DateFilter
import notion.api.v1.model.databases.query.filter.condition.SelectFilter
import notion.api.v1.model.databases.query.filter.condition.TimestampFilter
import notion.api.v1.model.databases.query.sort.QuerySort
import notion.api.v1.model.pages.PageProperty
import notion.api.v1.request.search.SearchRequest
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

import java.text.SimpleDateFormat;

class ScheduleTest {
    @Test
    fun getTodayTasks() {
        val client = NotionClient(System.getenv("notion"));
        val notionHelper = NotionHelper(client);
//
//        val database = client.search(
//            query = "Tasks",
//            filter = SearchRequest.SearchFilter("database", property = "object")
//        ).results.find { it.asDatabase().properties.containsKey("Name") }?.asDatabase()
//            ?: throw IllegalStateException("Could not find database")
//        val nestedCompoundQuery1 =
//            CompoundFilter(
//                and =
//                listOf(
//                    PropertyFilter(
//                        property = "Status",
//                        select = SelectFilter(doesNotEqual = "Completed")
//                    ),
//                    PropertyFilter(
//                        property = "Remind_Date",
//                        date  = DateFilter(after = LocalDate.now(ZoneId.of("America/New_York")).minusDays(1).toString())
//                    ),
//                    PropertyFilter(
//                        property = "Remind_Date",
//                        date  = DateFilter(before = LocalDate.now(ZoneId.of("America/New_York")).plusDays(1).toString())
//                    ),
//                )
//            )
//
//
//        val queryResult =
//            client.queryDatabase(
//                databaseId = database.id,
//                filter = CompoundFilter(or = listOf(nestedCompoundQuery1)),
//                sorts =
//                listOf(
//                    QuerySort(property = "title"),
//                )
//            )
        for (result in notionHelper.getTodayTasks()) {
            println(result.name)
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

        val database = client.search(
            query = "Classes", filter = SearchRequest.SearchFilter("database", property = "object")
        ).results.find { it.asDatabase().properties.containsKey("Name") }?.asDatabase()
            ?: throw IllegalStateException("Could not find database")
        val queryResult = client.queryDatabase(
            databaseId = database.id, filter = PropertyFilter(
                property = "Duration", select = SelectFilter(equals = "Summer 2023")
            ), sorts = listOf(
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

        for (res in notionHelper.getTodayTasks()) {
            val time = res.remindDate.getLocalDateTime()
            if (time != null) {
                val timeFrame = TimeFrame(time.toLocalTime(), time.toLocalTime().plusMinutes(60))
                scheduleService.addAction(HardAction(res.name, timeFrame, arrayOf(LocalDate.now().dayOfWeek)))
            }

        }

        println("Course List")
        for (course in courses) {
            scheduleService.addAction(course)
            println("${course.content}: ${course.timeFrame}")
        }

        println("Tuesday Schedule")
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