package sport.news.api.sport.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import sport.news.api.sport.dto.AuthorStatsDto
import sport.news.api.sport.services.AuthorStatsService
import java.time.LocalDate

@RestController
@RequestMapping("/authors")
class AuthorStatsController(
    private val authorStatsService: AuthorStatsService,
) {
    @GetMapping("/stats")
    fun getAuthorsStats(
        @RequestParam(defaultValue = "20") limit: Int
    ): List<AuthorStatsDto> {
        return authorStatsService.getAuthorsStats(limit)
    }
}
