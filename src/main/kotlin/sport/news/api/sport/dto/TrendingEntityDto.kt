package sport.news.api.sport.dto

data class TrendingEntityDto(
    val entity: String,
    val todayCount: Int,
    val baselineMean: Double,
    val zScore: Double,
    val growthFactor: Double,
)
