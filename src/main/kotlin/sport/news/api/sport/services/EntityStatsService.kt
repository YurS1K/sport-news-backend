package sport.news.api.sport.services

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
) {
    @Transactional(readOnly = true)
    fun getTopEntitiesByDate(limit: Int): List<EntityCountDto> {
        val oneWeekAgo = LocalDateTime.now().minusDays(7)
        val recentNews = newsRepository.findAllFromLastWeek(oneWeekAgo)

        return recentNews
            .flatMap { it.entities }
            .groupingBy { it }
            .eachCount()
            .map { (entity, count) -> EntityCountDto(entity, count.toLong()) }
            .sortedByDescending { it.count }
            .take(limit)
    }

    @Transactional(readOnly = true)
    fun findNewsByEntity(
        entityName: String,
        days: Int = 7,
    ): List<News> {
        val from = LocalDateTime.now().minusDays(days.toLong())
        val newsList = newsRepository.findAllFromLastWeek(from)
        return newsList.filter { entityName in it.entities }
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
