package sport.news.api.sport.dto

data class AuthorStatsDto(
    val author: String,
    val totalNews: Int,
    val avgSentiment: Double,
    val topEntities: List<EntityCountDto>,
    val sourceDistribution: Map<String, Int>,
)
