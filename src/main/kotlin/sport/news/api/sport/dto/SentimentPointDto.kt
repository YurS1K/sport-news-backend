package sport.news.api.sport.dto

import java.time.LocalDate

data class SentimentPointDto(
    val date: LocalDate,
    val positive: Int,
    val negative: Int,
    val neutral: Int,
    val total: Int,
)
