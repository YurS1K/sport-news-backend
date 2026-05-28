package sport.news.api.sport.services

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sport.news.api.sport.dto.EntityCountDto
import sport.news.api.sport.dto.SentimentPointDto
import sport.news.api.sport.dto.SentimentTimeseriesDto
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
        toDate: LocalDateTime
    ): List<News> {
        return newsRepository.findByEntityBetweenDates(entityName, fromDate, toDate)
    }

    @Transactional(readOnly = true)
    fun findNewsByEntityPaged(
        entityName: String,
        fromDate: LocalDateTime,
        toDate: LocalDateTime,
        sentiment: String?,
        pageable: Pageable
    ): Page<News> {
        // преобразуем sentiment в верхний регистр, чтобы соответствовало БД (POSITIVE, NEGATIVE, NEUTRAL)
        val sentimentParam = sentiment?.uppercase()?.takeIf { it in setOf("POSITIVE", "NEGATIVE", "NEUTRAL") }
        return newsRepository.findByEntityBetweenDatesAndSentiment(
            entityName, fromDate, toDate, sentimentParam, pageable
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

        val points = generateSequence(startDate) { it.plusDays(1) }
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
}