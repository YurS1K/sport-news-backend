package sport.news.api.sport.services

import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sport.news.api.sport.dto.TrendingEntityDto
import sport.news.api.sport.repositories.NewsRepository
import java.time.LocalDate

private const val Z_SCORE_THRESHOLD = 2.0
private const val Z_SCORE_CAP = 99.9

@Service
class TrendDetectionService(
    private val newsRepository: NewsRepository,
) {
    @Transactional(readOnly = true)
    fun getTrendingEntities(
        limit: Int = 10,
        baselineDays: Int = 30,
    ): List<TrendingEntityDto> {
        val today = LocalDate.now()
        val from = today.minusDays(baselineDays.toLong()).atStartOfDay()

        val allNews = newsRepository.findAllFromLastWeek(from)

        val entityDayCounts = mutableMapOf<String, MutableMap<LocalDate, Int>>()
        for (news in allNews) {
            val day = news.date.toLocalDate()
            for (entity in news.entities) {
                entityDayCounts.getOrPut(entity) { mutableMapOf() }.merge(day, 1, Int::plus)
            }
        }

        return entityDayCounts.entries
            .mapNotNull { (entity, dayCounts) ->
                val todayCount = dayCounts[today] ?: 0
                if (todayCount == 0) return@mapNotNull null

                val baselineCounts = (1..baselineDays).map { daysBack ->
                    dayCounts[today.minusDays(daysBack.toLong())] ?: 0
                }

                val mean = baselineCounts.average()
                val stdDev = sqrt(baselineCounts.sumOf { (it - mean).pow(2) } / baselineCounts.size)

                val zScore = when {
                    stdDev < 0.001 && todayCount > 0 -> min(todayCount.toDouble(), Z_SCORE_CAP)
                    stdDev < 0.001 -> 0.0
                    else -> ((todayCount - mean) / stdDev).coerceAtMost(Z_SCORE_CAP)
                }

                if (zScore < Z_SCORE_THRESHOLD) return@mapNotNull null

                TrendingEntityDto(
                    entity = entity,
                    todayCount = todayCount,
                    baselineMean = mean.round2(),
                    zScore = zScore.round2(),
                    growthFactor = if (mean > 0.0) (todayCount / mean).round2() else todayCount.toDouble(),
                )
            }
            .sortedByDescending { it.zScore }
            .take(limit)
    }

    private fun Double.round2() = (this * 100.0).roundToInt() / 100.0
}
