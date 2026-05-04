package sport.news.api.sport.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import sport.news.api.sport.dto.EntityCountDto
import sport.news.api.sport.entities.News
import sport.news.api.sport.services.EntityStatsService

@RestController
@RequestMapping("/entities")
class EntityController(
    private val entityStatsService: EntityStatsService,
) {
    @GetMapping("/top")
    fun getTopEntities(
        @RequestParam(defaultValue = "10") limit: Int,
    ): List<EntityCountDto> {
        return entityStatsService.getTopEntitiesByDate(limit)
    }

    @GetMapping("/news")
    fun getNewsByEntity(
        @RequestParam(name = "name") entityName: String,
        @RequestParam(defaultValue = "7") days: Int,
    ): List<News> {
        return entityStatsService.findNewsByEntity(entityName, days)
    }
}
