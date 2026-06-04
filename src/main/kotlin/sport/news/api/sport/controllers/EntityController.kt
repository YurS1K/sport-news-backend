package sport.news.api.sport.controllers

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import sport.news.api.sport.dto.CompareDto
import sport.news.api.sport.dto.EntityCountDto
import sport.news.api.sport.dto.SentimentTimeseriesDto
import sport.news.api.sport.dto.SourcesStatsDto
import sport.news.api.sport.entities.News
import sport.news.api.sport.services.EntityStatsService
import java.time.LocalDate

@RestController
@RequestMapping("/entities")
class EntityController(
    private val entityStatsService: EntityStatsService,
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

    @GetMapping("/compare")
    fun compareEntities(
        @RequestParam(name = "entity1") entity1: String,
        @RequestParam(name = "entity2") entity2: String,
        @RequestParam(defaultValue = "30") days: Int,
    ): CompareDto {
        return entityStatsService.getComparison(entity1, entity2, days)
    }

    @GetMapping("/sources-stats")
    fun getSourcesStats(
        @RequestParam(name = "name") entityName: String,
        @RequestParam(defaultValue = "30") days: Int,
    ): SourcesStatsDto {
        return entityStatsService.getSourcesStats(entityName, days)
    }
}
