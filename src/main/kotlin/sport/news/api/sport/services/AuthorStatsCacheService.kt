package sport.news.api.sport.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import sport.news.api.sport.dto.EntityCountDto
import sport.news.api.sport.entities.AuthorStatsCache
import sport.news.api.sport.repositories.AuthorStatsCacheRepository
import sport.news.api.sport.repositories.NewsRepository
import java.time.LocalDateTime

@Service
class AuthorStatsCacheService(
    private val newsRepository: NewsRepository,
    private val cacheRepository: AuthorStatsCacheRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = [Exception::class])
    suspend fun refreshCache() {
        logger.info("Начат пересчёт кэша авторов (последние 7 дней)")
        val startTime = System.currentTimeMillis()

        try {
            val oneWeekAgo = LocalDateTime.now().minusDays(7)
            val recentNews = newsRepository.findAllFromLastWeek(oneWeekAgo)

            val filteredNews = recentNews.filter {
                it.author.isNotBlank() && it.author != "Нет автора"
            }

            val groupedByAuthor = filteredNews.groupBy { it.author }

            val now = LocalDateTime.now()
            val newStats = groupedByAuthor.map { (author, authorNews) ->
                val total = authorNews.size
                val avgSent = authorNews.map { news ->
                    when (news.sentiment.uppercase()) {
                        "POSITIVE" -> 1.0
                        "NEGATIVE" -> -1.0
                        else -> 0.0
                    }
                }.average()

                val entityCounts = authorNews
                    .flatMap { it.entities }
                    .groupingBy { it }
                    .eachCount()
                    .entries
                    .sortedByDescending { it.value }
                    .take(5)
                    .map { EntityCountDto(it.key, it.value.toLong()) }

                val sourceDist = authorNews.groupBy { it.source }.mapValues { it.value.size }

                AuthorStatsCache(
                    author = author,
                    totalNews = total,
                    avgSentiment = avgSent,
                    topEntitiesJson = objectMapper.writeValueAsString(entityCounts),
                    sourceDistributionJson = objectMapper.writeValueAsString(sourceDist),
                    calculatedAt = now
                )
            }

            cacheRepository.deleteByCalculatedAtBefore(now)
            cacheRepository.saveAll(newStats)

            val duration = System.currentTimeMillis() - startTime
            logger.info("Кэш авторов обновлён: ${newStats.size} записей за ${duration}мс")
        } catch (e: Exception) {
            logger.error("Ошибка при обновлении кэша авторов", e)
        }
    }

    @Transactional(readOnly = true)
    fun getCurrentCache(): List<AuthorStatsCache> {
        return cacheRepository.findAllCurrent()
    }

    @Transactional
    @PostConstruct
    fun initCache() {
        scope.launch { refreshCache() }
    }
}