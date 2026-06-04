package sport.news.api.sport.dto

import java.time.LocalDate

data class DailyStatDto(
    val date: LocalDate,
    val mentions: Int,
    val avgSentiment: Double,
)
