package sport.news.api.sport.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import sport.news.api.sport.dto.TrendingEntityDto
import sport.news.api.sport.services.TrendDetectionService

@RestController
@RequestMapping("/trends")
class TrendController(
    private val trendDetectionService: TrendDetectionService,
) {
    @GetMapping("/today")
    fun getTrendingToday(
        @RequestParam(defaultValue = "10") limit: Int,
        @RequestParam(defaultValue = "30") baselineDays: Int,
    ): List<TrendingEntityDto> {
        return trendDetectionService.getTrendingEntities(limit, baselineDays)
    }
}
