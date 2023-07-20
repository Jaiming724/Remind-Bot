package dev.scratch.remindbot

import dev.scratch.scheduler.model.actions.HardAction
import dev.scratch.scheduler.util.DateTimeOptional
import dev.scratch.scheduler.util.SimpleDate
import dev.scratch.scheduler.util.SimpleDateTime
import dev.scratch.scheduler.util.TimeFrame
import notion.api.v1.NotionClient
import notion.api.v1.model.databases.DatabaseProperty
import notion.api.v1.model.databases.query.filter.PropertyFilter
import notion.api.v1.model.databases.query.filter.condition.SelectFilter
import notion.api.v1.model.databases.query.sort.QuerySort
import notion.api.v1.model.databases.query.sort.QuerySortDirection
import notion.api.v1.model.databases.query.sort.QuerySortTimestamp
import notion.api.v1.model.pages.PageParent
import notion.api.v1.model.pages.PageProperty
import notion.api.v1.model.search.DatabaseSearchResult
import notion.api.v1.request.search.SearchRequest
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*


class NotionHelper constructor(private val client: NotionClient) {
    private val database: DatabaseSearchResult


    init {
        database = client.search(
            query = "Tasks",
            filter = SearchRequest.SearchFilter("database", property = "object")
        ).results.find { it.asDatabase().properties.containsKey("Name") }?.asDatabase()
            ?: throw IllegalStateException("Could not find database")
    }

    fun getTodayTasks(): MutableList<Task> {
        val queryResult =
            client.queryDatabase(
                databaseId = database.id,
                filter = PropertyFilter(
                    property = "Status",
                    select = SelectFilter(doesNotEqual = "Completed")
                ),
                sorts =
                listOf(
                    QuerySort(property = "title"),
                    QuerySort(
                        timestamp = QuerySortTimestamp.LastEditedTime,
                        direction = QuerySortDirection.Descending
                    )
                )
            )
        val list = mutableListOf<Task>()

        for (result in queryResult.results) {

            val map = result.properties

            val name = map["Name"]?.title?.get(0)?.plainText ?: "UNKNOWN"


            val remindTime = map["Remind_Date"]?.date?.start ?: LocalDate.now().toString()
            val dueDate = map["Due Date"]?.date?.start ?: "UNKNOWN"

            var completed = false
            var received = false
            val i = map["Status"]?.select!!
            if (i.name == "Backlog")
                continue
            else if (i.name == "Completed")
                completed = true
            else if (i.name == "On-Going")
                received = true
            val t = DateTimeOptional(remindTime)

            when (t) {
                is SimpleDateTime -> {
                    val task = Task(name, t, dueDate, completed, received, result.id)
                    if (t.dateTime.toLocalDate() == LocalDate.now()) {
                        list.add(task)
                    }
                }

                is SimpleDate -> {
                    if (t.date == LocalDate.now()) {
                        val task = Task(name, t, dueDate, completed, received, result.id)
                        list.add(task)

                    }
                }

                else -> {
                    println("unknown")
                }
            }

        }
        return list
    }

    fun getClasses(semester: String): MutableList<HardAction> {
        val courses: MutableList<HardAction> = mutableListOf()

        val database = client.search(
            query = "Classes", filter = SearchRequest.SearchFilter("database", property = "object")
        ).results.find { it.asDatabase().properties.containsKey("Name") }?.asDatabase()
            ?: throw IllegalStateException("Could not find database")
        val queryResult = client.queryDatabase(
            databaseId = database.id, filter = PropertyFilter(
                property = "Duration", select = SelectFilter(equals = semester)
            ), sorts = listOf(
                QuerySort(property = "title"),
            )
        )
        for (result in queryResult.results) {
            val map = result.properties
            val content: String? = map["Name"]?.title?.get(0)?.plainText
            val start = LocalDateTime.parse(
                map["Time"]?.date?.start!!,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            )
            val end = LocalDateTime.parse(
                map["Time"]?.date?.end!!,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            )

            val mutableList: MutableList<DayOfWeek> = mutableListOf()
            for (i in map["Days"]?.multiSelect!!) {
                mutableList.add(getDayOfWeek(i.name!!))
            }
            val timeFrame = TimeFrame(start.toLocalTime(), end.toLocalTime())
            courses.add(HardAction(content!!, timeFrame, mutableList.toTypedArray()))
        }
        return courses
    }

    fun getTasks(): MutableList<Task> {
        val queryResult =
            client.queryDatabase(
                databaseId = database.id,
                filter = PropertyFilter(
                    property = "Status",
                    select = SelectFilter(doesNotEqual = "Completed")
                ),
                sorts =
                listOf(
                    QuerySort(property = "title"),
                    QuerySort(
                        timestamp = QuerySortTimestamp.LastEditedTime,
                        direction = QuerySortDirection.Descending
                    )
                )
            )
        val list = mutableListOf<Task>()

        for (result in queryResult.results) {

            val map = result.properties

            val name = map["Name"]?.title?.get(0)?.plainText ?: "UNKNOWN"


            val remindTime = map["Remind_Date"]?.date?.start ?: "UNKNOWN"
            val dueDate = map["Due Date"]?.date?.start ?: "UNKNOWN"

            var completed = false
            var received = false
            val i = map["Status"]?.select!!
            if (i.name == "Backlog")
                continue
            else if (i.name == "Completed")
                completed = true
            else if (i.name == "On-Going")
                received = true


            val task = Task(name, DateTimeOptional(remindTime), dueDate, completed, received, result.id)
            list.add(task)
        }
        return list
    }


    fun addTask(task: Task): String {
        var status: DatabaseProperty.Select.Option? = null

        if (task.completed) {
            status = (DatabaseProperty.Select.Option(name = "Completed"))
        } else if (task.received) {
            status = (DatabaseProperty.Select.Option(name = "On-Going"))
        }
        val newPage = client.createPage(
            parent = PageParent.database(database.id),
            properties = mapOf(
                "Name" to PageProperty(
                    title = listOf(
                        PageProperty.RichText(
                            text = PageProperty.RichText.Text(
                                content = task.name
                            )
                        )
                    )
                ),
                "Status" to PageProperty(select = status),
                "Remind_Date" to PageProperty(date = PageProperty.Date(task.remindDate.toString())),
                "Due Date" to PageProperty(date = PageProperty.Date(task.dueDate)),
            )
        )
        return newPage.id
    }

    fun updateTaskRemindDate(id: String): String {
        val timeOffset: String = (ZoneOffset.systemDefault().rules.getOffset(Instant.now()).toString())
        return client.updatePage(
            pageId = id,
            properties = mapOf(
                "Remind_Date" to PageProperty(
                    date = PageProperty.Date(
                        LocalDateTime.now().plusMinutes(5).toString() + timeOffset
                    )
                ),
            )
        ).id
    }

    fun markAsReceived(id: String): String {
        val status = (DatabaseProperty.Select.Option(name = "On-Going"))
        return client.updatePage(
            pageId = id,
            properties = mapOf(
                "Status" to PageProperty(select = status),
            )
        ).id
    }

    fun markAsCompleted(id: String): String {
        val status = (DatabaseProperty.Select.Option(name = "Completed"))
        return client.updatePage(
            pageId = id,
            properties = mapOf(
                "Status" to PageProperty(select = status),
            )
        ).id
    }

    fun markAsNotStarted(id: String): String {
        val status = (DatabaseProperty.Select.Option(name = "Not-Started"))
        return client.updatePage(
            pageId = id,
            properties = mapOf(
                "Status" to PageProperty(select = status),
            )
        ).id
    }

    fun removeTask(id: String) {
        client.deleteBlock(id)
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