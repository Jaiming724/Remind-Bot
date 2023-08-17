import notion.api.v1.NotionClient
import notion.api.v1.model.databases.query.sort.QuerySort
import notion.api.v1.model.pages.PageProperty
import notion.api.v1.request.search.SearchRequest
import org.jasypt.util.text.AES256TextEncryptor
import org.junit.jupiter.api.Test
import java.util.*

class EncryptionTest {
    @Test
    fun encryption() {

        val password = ""
        val choice = "Encypt"
        if (password == "") {
            throw IllegalStateException("No password")
        }
        val client = NotionClient(System.getenv("notion"));

        val database = client.search(
            query = "Yes",
            filter = SearchRequest.SearchFilter("database", property = "object")
        ).results.find { it.asDatabase().properties.containsKey("Name") }?.asDatabase()

            ?: throw IllegalStateException("Could not find database")
        val queryResult =
            client.queryDatabase(
                databaseId = database.id,
                sorts =
                listOf(
                    QuerySort(property = "title"),
                )
            )

        val textEncryptor = AES256TextEncryptor();
        textEncryptor.setPassword(password);
        for (result in queryResult.results) {
            val map = result.properties
            if (map.isNotEmpty() && map["Name"]?.title?.isNotEmpty() == true) {
                val name = map["Name"]?.title?.get(0)?.plainText ?: "UNKNOWN"
                if (choice == "Encrypt") {
                    val myEncryptedText = textEncryptor.encrypt(name);
                    client.updatePage(
                        pageId = result.id,
                        properties = mapOf(
                            "Name" to PageProperty(
                                title = listOf(PageProperty.RichText(text = PageProperty.RichText.Text(content = myEncryptedText))),
                            ),
                        )
                    ).id
                } else {
                    val plainText = textEncryptor.decrypt(name);
                    client.updatePage(
                        pageId = result.id,
                        properties = mapOf(
                            "Name" to PageProperty(
                                title = listOf(PageProperty.RichText(text = PageProperty.RichText.Text(content = plainText))),
                            ),
                        )
                    ).id
                }
            }

        }

    }
}