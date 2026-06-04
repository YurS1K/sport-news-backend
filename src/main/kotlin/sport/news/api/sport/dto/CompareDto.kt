package sport.news.api.sport.dto

import java.time.LocalDate

data class CompareDto(
    val entity1: String,
    val entity2: String,
    val from: LocalDate,
    val to: LocalDate,
    val data1: List<DailyStatDto>,
    val data2: List<DailyStatDto>,
)
