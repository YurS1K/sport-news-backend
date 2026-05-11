package sport.news.api.sport.entities

import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

@Entity
@Table(name = "news", uniqueConstraints = [UniqueConstraint(columnNames = ["link"])])
data class News(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val title: String,
    @Column(unique = true)
    val link: String,
    @ElementCollection
    val tags: List<String> = emptyList(),
    val date: LocalDateTime,
    val author: String,
    @Column(columnDefinition = "TEXT")
    val text: String,
    @ElementCollection
    val entities: List<String> = emptyList(),
    val sentiment: String,
    val source: String,
) {
    companion object {
        private val dateTimeFormatter =
            DateTimeFormatterBuilder()
                .appendPattern("HH:mm ")
                .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, java.time.format.SignStyle.NOT_NEGATIVE)
                .appendPattern(".")
                .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, java.time.format.SignStyle.NOT_NEGATIVE)
                .appendPattern(".")
                .appendValue(ChronoField.YEAR, 4)
                .toFormatter()

        fun fromCsvRow(
            title: String,
            link: String,
            tagsStr: String,
            dateStr: String,
            author: String,
            text: String,
            entitiesStr: String,
            sentiment: String,
            source: String,
        ): News {
            fun parsePythonStyleList(s: String): List<String> {
                return """'([^']*)'""".toRegex()
                    .findAll(s)
                    .map { it.groupValues[1] }
                    .toList()
            }

            return News(
                title = title.trim(),
                link = link.trim(),
                tags = parsePythonStyleList(tagsStr),
                date = LocalDateTime.parse(dateStr.trim(), dateTimeFormatter),
                author = author.trim(),
                text = text.trim(),
                entities = parsePythonStyleList(entitiesStr),
                sentiment = sentiment.trim(),
                source = source.trim(),
            )
        }
    }
}
