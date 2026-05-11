package sport.news.api.sport.dto

import java.time.LocalDate

data class SentimentTimeseriesDto(
    val entity: String,
    val from: LocalDate,
    val to: LocalDate,
    val points: List<SentimentPointDto>,
)
