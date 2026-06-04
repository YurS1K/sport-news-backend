package sport.news.api.sport.services

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sport.news.api.sport.dto.CompareDto
import sport.news.api.sport.dto.DailyStatDto
import sport.news.api.sport.dto.EntityCountDto
import sport.news.api.sport.dto.SentimentPointDto
import sport.news.api.sport.dto.SentimentTimeseriesDto
import sport.news.api.sport.dto.SourcesStatsDto
import sport.news.api.sport.entities.News
import sport.news.api.sport.repositories.NewsRepository
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class EntityStatsService(
    private val newsRepository: NewsRepository,
    private val topEntitiesCacheService: TopEntitiesCacheService,
) {
    @Transactional(readOnly = true)
    fun getTopEntitiesByDate(limit: Int): List<EntityCountDto> {
        val cachedStats = topEntitiesCacheService.getCurrentCache()
        return cachedStats
            .sortedByDescending { it.count }
            .take(limit)
            .map { EntityCountDto(it.entity, it.count) }
    }

    @Transactional(readOnly = true)
    fun findNewsByEntity(
        entityName: String,
        fromDate: LocalDateTime,
        toDate: LocalDateTime,
    ): List<News> {
        return newsRepository.findByEntityBetweenDates(entityName, fromDate, toDate)
    }

    @Transactional(readOnly = true)
    fun findNewsByEntityPaged(
        entityName: String,
        fromDate: LocalDateTime,
        toDate: LocalDateTime,
        sentiment: String?,
        pageable: Pageable,
    ): Page<News> {
        val sentimentParam = sentiment?.uppercase()?.takeIf { it in setOf("POSITIVE", "NEGATIVE", "NEUTRAL") }
        return newsRepository.findByEntityBetweenDatesAndSentiment(
            entityName,
            fromDate,
            toDate,
            sentimentParam,
            pageable,
        )
    }

    @Transactional(readOnly = true)
    fun getSentimentTimeseries(
        entityName: String,
        days: Int,
    ): SentimentTimeseriesDto {
        val today = LocalDate.now()
        val startDate = today.minusDays(days.toLong())
        val from = startDate.atStartOfDay()

        val news = newsRepository.findByEntityFromDate(entityName, from)
        val grouped = news.groupBy { it.date.toLocalDate() }

        val points =
            generateSequence(startDate) { it.plusDays(1) }
                .takeWhile { !it.isAfter(today) }
                .map { date ->
                    val dayNews = grouped[date].orEmpty()
                    SentimentPointDto(
                        date = date,
                        positive = dayNews.count { it.sentiment == "POSITIVE" },
                        negative = dayNews.count { it.sentiment == "NEGATIVE" },
                        neutral = dayNews.count { it.sentiment == "NEUTRAL" },
                        total = dayNews.size,
                    )
                }
                .toList()

        return SentimentTimeseriesDto(
            entity = entityName,
            from = startDate,
            to = today,
            points = points,
        )
    }

    @Transactional(readOnly = true)
    fun getComparison(
        entity1: String,
        entity2: String,
        days: Int,
    ): CompareDto {
        val today = LocalDate.now()
        val startDate = today.minusDays(days.toLong())
        val from = startDate.atStartOfDay()
        val to = today.atTime(23, 59, 59)

        val news1 = newsRepository.findByEntityBetweenDates(entity1, from, to)
        val news2 = newsRepository.findByEntityBetweenDates(entity2, from, to)

        fun buildDailyStats(newsList: List<News>): Map<LocalDate, Pair<Int, Double>> {
            return newsList.groupBy { it.date.toLocalDate() }.mapValues { (_, list) ->
                val mentions = list.size
                val avgSentiment =
                    list.map { news ->
                        when (news.sentiment.uppercase()) {
                            "POSITIVE" -> 1.0
                            "NEUTRAL" -> 0.0
                            "NEGATIVE" -> -1.0
                            else -> 0.0
                        }
                    }.average()
                mentions to avgSentiment
            }
        }

        val stats1 = buildDailyStats(news1)
        val stats2 = buildDailyStats(news2)

        val allDates =
            generateSequence(startDate) { it.plusDays(1) }
                .takeWhile { !it.isAfter(today) }
                .toList()

        fun toDailyStatList(stats: Map<LocalDate, Pair<Int, Double>>): List<DailyStatDto> {
            return allDates.map { date ->
                val (mentions, avgSent) = stats[date] ?: (0 to 0.0)
                DailyStatDto(date, mentions, avgSent)
            }
        }

        return CompareDto(
            entity1 = entity1,
            entity2 = entity2,
            from = startDate,
            to = today,
            data1 = toDailyStatList(stats1),
            data2 = toDailyStatList(stats2),
        )
    }

    fun getSourcesStats(
        entityName: String,
        days: Int,
    ): SourcesStatsDto {
        val today = LocalDate.now()
        val startDate = today.minusDays(days.toLong())
        val from = startDate.atStartOfDay()
        val to = today.atTime(23, 59, 59)
        val news = newsRepository.findByEntityBetweenDates(entityName, from, to)
        val riaCount = news.count { it.source == "RIA" }
        val championatCount = news.count { it.source == "CHAMPIONAT" }
        return SourcesStatsDto(ria = riaCount.toLong(), championat = championatCount.toLong())
    }
}
