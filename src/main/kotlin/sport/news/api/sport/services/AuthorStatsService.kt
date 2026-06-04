package sport.news.api.sport.services

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sport.news.api.sport.dto.AuthorStatsDto
import sport.news.api.sport.dto.EntityCountDto
import sport.news.api.sport.entities.AuthorStatsCache
import sport.news.api.sport.repositories.AuthorStatsCacheRepository

@Service
class AuthorStatsService(
    private val cacheService: AuthorStatsCacheService,
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
) {

    @Transactional(readOnly = true)
    fun getAuthorsStats(limit: Int = 20): List<AuthorStatsDto> {
        return cacheService.getCurrentCache()
            .sortedByDescending { it.totalNews }
            .take(limit)
            .map { cache ->
                AuthorStatsDto(
                    author = cache.author,
                    totalNews = cache.totalNews,
                    avgSentiment = cache.avgSentiment,
                    topEntities = objectMapper.readValue(cache.topEntitiesJson),
                    sourceDistribution = objectMapper.readValue(cache.sourceDistributionJson)
                )
            }
    }
}