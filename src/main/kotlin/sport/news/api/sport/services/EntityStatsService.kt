package sport.news.api.sport.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sport.news.api.sport.dto.EntityCountDto
import sport.news.api.sport.entities.News
import sport.news.api.sport.repositories.NewsRepository
import java.time.LocalDateTime

@Service
class EntityStatsService(
    private val newsRepository: NewsRepository
) {

    @Transactional(readOnly = true)
    fun getTopEntitiesLastWeek(limit: Int): List<EntityCountDto> {
        val oneWeekAgo = LocalDateTime.now().minusDays(100)
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
    fun findNewsByEntity(entityName: String, days: Int = 7): List<News> {
        val from = LocalDateTime.now().minusDays(days.toLong())
        val newsList = newsRepository.findAllFromLastWeek(from)
        return newsList.filter { entityName in it.entities }
    }
}