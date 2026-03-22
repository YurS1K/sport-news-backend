package sport.news.api.sport.services

import com.opencsv.CSVReader
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import sport.news.api.sport.entities.News
import java.io.InputStreamReader
import java.time.format.DateTimeFormatter

@Service
class NewsCsvService {

    private val csvPath = "parsed_data.csv"
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy")

    fun readNewsFromCsv(): List<News> {
        val resource = ClassPathResource(csvPath)
        val inputStream = resource.inputStream
        val reader = CSVReader(InputStreamReader(inputStream))
        val lines = reader.readAll()
        reader.close()

        val headers = lines.first()
        val headerMap = headers.mapIndexed { index, name -> name.trim() to index }.toMap()
        val dataLines = lines.drop(1)

        return dataLines.mapNotNull { line ->
            try {
                parseLineToNews(line, headerMap)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun parseLineToNews(line: Array<String>, headerMap: Map<String, Int>): News {
        fun getColumn(name: String): String = line[headerMap[name]!!].trim()

        fun parsePythonStyleList(s: String): List<String> =
            """'([^']*)'""".toRegex().findAll(s).map { it.groupValues[1] }.toList()

        return News.fromCsvRow(
            title = getColumn("title"),
            link = getColumn("link"),
            tagsStr = getColumn("tags"),
            dateStr = getColumn("date"),
            author = getColumn("author"),
            text = getColumn("text"),
            entitiesStr = getColumn("entities"),
            sentiment = getColumn("sentiment"),
            source = getColumn("source"),

        )
    }
}