package sport.news.api.sport.controllers

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.*
import sport.news.api.sport.dto.EntityCountDto
import sport.news.api.sport.dto.SentimentTimeseriesDto
import sport.news.api.sport.entities.News
import sport.news.api.sport.services.EntityStatsService
import sport.news.api.sport.services.TopEntitiesCacheService
import java.time.LocalDate

@RestController
@RequestMapping("/entities")
class EntityController(
    private val entityStatsService: EntityStatsService,
    private val topEntitiesCacheService: TopEntitiesCacheService,
) {
    @GetMapping("/top")
    fun getTopEntities(
        @RequestParam(defaultValue = "5") limit: Int,
    ): List<EntityCountDto> {
        return entityStatsService.getTopEntitiesByDate(limit)
    }

    @GetMapping("/news")
    fun getNewsByEntity(
        @RequestParam(name = "name") entityName: String,
        @RequestParam from: LocalDate,
        @RequestParam to: LocalDate,
    ): List<News> {
        val fromDateTime = from.atStartOfDay()
        val toDateTime = to.atTime(23, 59, 59)
        return entityStatsService.findNewsByEntity(entityName, fromDateTime, toDateTime)
    }

    @GetMapping("/news/paged")
    fun getNewsByEntityPaged(
        @RequestParam(name = "name") entityName: String,
        @RequestParam from: LocalDate,
        @RequestParam to: LocalDate,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int,
        @RequestParam(required = false) sentiment: String?,
    ): Page<News> {
        val fromDateTime = from.atStartOfDay()
        val toDateTime = to.atTime(23, 59, 59)
        val pageable = PageRequest.of(page, size, Sort.by("date").descending())
        return entityStatsService.findNewsByEntityPaged(entityName, fromDateTime, toDateTime, sentiment, pageable)
    }

    @GetMapping("/sentiment")
    fun getSentimentTimeseries(
        @RequestParam(name = "name") entityName: String,
        @RequestParam(defaultValue = "30") days: Int,
    ): SentimentTimeseriesDto {
        return entityStatsService.getSentimentTimeseries(entityName, days)
    }
}