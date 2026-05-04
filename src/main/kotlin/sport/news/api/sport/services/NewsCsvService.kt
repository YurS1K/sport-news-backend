package sport.news.api.sport.services

import com.opencsv.CSVReader
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import sport.news.api.sport.entities.News
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Paths

@Service
class NewsCsvService {
    private val resourcesPath: String by lazy {
        val projectRoot = Paths.get("").toAbsolutePath().toString()
        "$projectRoot/src/main/resources"
    }

    private val championatCsvPath = "$resourcesPath\\championat_parsed_data.csv"
    private val riaCsvPath = "$resourcesPath\\ria_parsed_data.csv"

    private val logger = LoggerFactory.getLogger(javaClass)

    fun readNewsFromChampionatCsv(): List<News> {
        return readFromCSV(championatCsvPath)
    }

    fun readNewsFromRiaCsv(): List<News> {
        return readFromCSV(riaCsvPath)
    }

    private fun readFromCSV(path: String): List<News> {
        val file = File(path)

        if (!file.exists()) {
            logger.warn("Файл не существует: ${file.absolutePath}")
            return emptyList()
        }

        if (file.length() == 0L) {
            logger.warn("Файл пуст: ${file.absolutePath}")
            return emptyList()
        }

        logger.info("Чтение файла: ${file.absolutePath}")

        val reader = CSVReader(InputStreamReader(file.inputStream(), Charsets.UTF_8))
        val lines = reader.readAll()
        reader.close()

        if (lines.isEmpty()) {
            logger.warn("CSV файл не содержит строк: $path")
            return emptyList()
        }

        val headers = lines.first()
        val headerMap = headers.mapIndexed { index, name -> name.trim() to index }.toMap()
        val dataLines = lines.drop(1)

        return dataLines.mapNotNull { line ->
            try {
                parseLineToNews(line, headerMap)
            } catch (e: Exception) {
                logger.error("Ошибка парсинга строки: ${line.joinToString()}", e)
                null
            }
        }
    }

    private fun parseLineToNews(
        line: Array<String>,
        headerMap: Map<String, Int>,
    ): News {
        fun getColumn(name: String): String = line[headerMap[name]!!].trim()

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
