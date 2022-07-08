package dev.scratch.remindbot

import notion.api.v1.NotionClient
import notion.api.v1.http.OkHttp4Client
import notion.api.v1.model.databases.query.filter.PropertyFilter
import notion.api.v1.model.databases.query.filter.condition.CheckboxFilter
import notion.api.v1.model.databases.query.sort.QuerySort
import notion.api.v1.model.databases.query.sort.QuerySortDirection
import notion.api.v1.model.databases.query.sort.QuerySortTimestamp
import notion.api.v1.model.pages.PageParent
import notion.api.v1.model.pages.PageProperty
import notion.api.v1.model.search.DatabaseSearchResult
import notion.api.v1.request.search.SearchRequest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class NotionHelper constructor(private val client: NotionClient) {
    private val database: DatabaseSearchResult

    init {
        database = client.search(
            query = "Tasks",
            filter = SearchRequest.SearchFilter("database", property = "object")
        ).results.find { it.asDatabase().properties.containsKey("Name") }?.asDatabase()
            ?: throw IllegalStateException("Could not find database")
    }

    fun getTasks(): MutableList<Task> {
        val queryResult =
            client.queryDatabase(
                databaseId = database.id,
                filter = PropertyFilter(property = "Completed", checkbox = CheckboxFilter(false)),
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
            val completed = map["Completed"]?.checkbox ?: false
            val received = map["Received"]?.checkbox ?: false
            val task = Task(name, remindTime, dueDate, completed, received, result.id)
            list.add(task)
        }
        return list
    }

    fun addTask(task: Task): String {
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
                "Received" to PageProperty(checkbox = task.received),
                "Completed" to PageProperty(checkbox = task.completed),
                "Remind_Date" to PageProperty(date = PageProperty.Date(task.remindDate)),
                "Due Date" to PageProperty(date = PageProperty.Date(task.dueDate)),
            )
        )
        return newPage.id
    }

    fun updateTaskRemindDate(id: String): String {
        val timeOffset: String = (ZoneOffset.systemDefault().getRules().getOffset(Instant.now()).toString())

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
        return client.updatePage(
            pageId = id,
            properties = mapOf(
                "Received" to PageProperty(checkbox = true)
            )
        ).id
    }

    fun markAsCompleted(id: String): String {
        return client.updatePage(
            pageId = id,
            properties = mapOf(
                "Completed" to PageProperty(checkbox = true)
            )
        ).id
    }

    fun removeTask(id: String) {
        client.deleteBlock(id)
    }


}