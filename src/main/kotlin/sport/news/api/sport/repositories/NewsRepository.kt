package sport.news.api.sport.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import sport.news.api.sport.entities.News
import java.time.LocalDateTime

@Repository
interface NewsRepository : JpaRepository<News, Long> {
    fun findFirstByLink(link: String): News?

    @Query("SELECT n FROM News n WHERE n.date >= :fromDate")
    fun findAllFromLastWeek(
        @Param("fromDate") fromDate: LocalDateTime,
    ): List<News>
}
