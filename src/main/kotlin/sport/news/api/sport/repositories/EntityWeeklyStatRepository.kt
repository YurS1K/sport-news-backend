package sport.news.api.sport.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import sport.news.api.sport.entities.EntityWeeklyStat
import java.time.LocalDateTime

interface EntityWeeklyStatRepository : JpaRepository<EntityWeeklyStat, Long> {
    @Query("SELECT e FROM EntityWeeklyStat e WHERE e.calculatedAt = (SELECT MAX(e2.calculatedAt) FROM EntityWeeklyStat e2)")
    fun findAllCurrent(): List<EntityWeeklyStat>

    fun deleteByCalculatedAtBefore(date: LocalDateTime)
}
