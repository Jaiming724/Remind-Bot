import dev.scratch.remindbot.NotionHelper
import dev.scratch.scheduler.model.Schedule
import dev.scratch.scheduler.model.actions.HardAction
import dev.scratch.scheduler.service.Scheduler
import dev.scratch.scheduler.util.SimpleDateTime
import dev.scratch.scheduler.util.TimeFrame
import notion.api.v1.NotionClient
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.time.*
import java.time.temporal.TemporalAdjusters
import java.util.*

class ScheduleTest {
    @Test
    fun getTodayTasks() {
        val client = NotionClient(System.getenv("notion"))
        val notionHelper = NotionHelper(client)

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
        val client = NotionClient(System.getenv("notion"))
        val notionHelper = NotionHelper(client)

        val scheduleService = Scheduler()

        for (res in notionHelper.getTasks()) {
            when (val time = res.remindDate) {
                is SimpleDateTime -> {
                    println(time.dateTime.toOffsetTime().toString())
                    val start = SimpleDateTime(time.dateTime.toString())
                    println(start)
                }
                else -> {}
            }
        }

//        println("Course List")
//        for (course in notionHelper.getClasses("Fall 2023")) {
//            scheduleService.addAction(course)
//            //println("${course.content}: ${course.timeFrame}")
//        }
//
//        val map: Map<DayOfWeek, Schedule> = scheduleService.schedule()
//        println(LocalDate.now().dayOfWeek)
////        map[LocalDate.now().dayOfWeek]?.schedule?.forEach {
////            val hardAction = it.value as HardAction
////            println("${it.value.content} ${hardAction.timeFrame.start}")
////        }
//        map.forEach { (t, u) ->
//            println(t)
//            u.schedule.forEach {
//                val hardAction = it.value as HardAction
//                println("${it.value.content} ${getNextTime(hardAction.timeFrame.start, t)}")
//            }
//        }


    }

    fun getNextTime(time: LocalTime, dayOfWeek: DayOfWeek): OffsetDateTime {
        var ld = LocalDate.now()
        ld = ld.with(TemporalAdjusters.nextOrSame(dayOfWeek))
        val nextTime = LocalDateTime.of(ld, time)
        val zoneId = ZoneId.systemDefault()

        return nextTime.atZone(zoneId).toOffsetDateTime();
    }
}