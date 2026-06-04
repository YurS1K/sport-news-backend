package sport.news.api.sport.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import sport.news.api.sport.entities.AuthorStatsCache
import java.time.LocalDateTime

interface AuthorStatsCacheRepository : JpaRepository<AuthorStatsCache, Long> {
    @Query("SELECT a FROM AuthorStatsCache a WHERE a.calculatedAt = (SELECT MAX(a2.calculatedAt) FROM AuthorStatsCache a2)")
    fun findAllCurrent(): List<AuthorStatsCache>

    fun deleteByCalculatedAtBefore(date: LocalDateTime)
}
