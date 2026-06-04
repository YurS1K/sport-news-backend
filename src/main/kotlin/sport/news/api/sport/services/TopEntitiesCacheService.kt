package sport.news.api.sport.services

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import sport.news.api.sport.entities.EntityWeeklyStat
import sport.news.api.sport.repositories.EntityWeeklyStatRepository
import sport.news.api.sport.repositories.NewsRepository
import java.time.LocalDateTime

@Service
class TopEntitiesCacheService(
    private val newsRepository: NewsRepository,
    private val cacheRepository: EntityWeeklyStatRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Асинхронный пересчёт кеша. Вызывается после добавления новых новостей.
     */
    @Transactional(
        propagation = Propagation.REQUIRES_NEW,
        rollbackFor = [Exception::class],
    )
    suspend fun refreshTopEntitiesCache() {
        logger.info("Начат пересчёт кеша топ-сущностей")
        val startTime = System.currentTimeMillis()

        try {
            val oneWeekAgo = LocalDateTime.now().minusDays(7)
            val recentNews = newsRepository.findAllFromLastWeek(oneWeekAgo)

            val counts =
                recentNews
                    .flatMap { it.entities }
                    .groupingBy { it }
                    .eachCount()
                    .map { (entity, count) -> entity to count.toLong() }

            val now = LocalDateTime.now()
            val newStats =
                counts.map { (entity, count) ->
                    EntityWeeklyStat(entity = entity, count = count, calculatedAt = now)
                }
            cacheRepository.saveAll(newStats)

            cacheRepository.deleteByCalculatedAtBefore(now)

            val duration = System.currentTimeMillis() - startTime
            logger.info("Кеш топ-сущностей обновлён: ${newStats.size} записей за ${duration}мс")
        } catch (e: Exception) {
            logger.error("Ошибка при пересчёте кеша топ-сущностей", e)
        }
    }

    /**
     * Получить текущий кеш (все сущности с их количеством).
     * Используется сервисом EntityStatsService.
     */
    @Transactional(readOnly = true)
    fun getCurrentCache(): List<EntityWeeklyStat> {
        return cacheRepository.findAllCurrent()
    }

    @Transactional
    @PostConstruct
    fun initCache() {
        scope.launch { refreshTopEntitiesCache() }
    }
}
