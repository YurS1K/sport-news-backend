package sport.news.api.sport.entities

import jakarta.persistence.*
import java.time.LocalDateTime

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
        private val dateTimeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy")

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
                date = LocalDateTime.parse(dateStr, dateTimeFormatter),
                author = author.trim(),
                text = text.trim(),
                entities = parsePythonStyleList(entitiesStr),
                sentiment = sentiment.trim(),
                source = source.trim(),
            )
        }
    }
}