package sport.news.api.sport.repositories

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import sport.news.api.sport.entities.News
import java.time.LocalDateTime

@Repository
interface NewsRepository : JpaRepository<News, Long> {
    fun findFirstByLink(link: String): News?

    @Query("SELECT n FROM News n LEFT JOIN FETCH n.entities WHERE n.date >= :fromDate")
    fun findAllFromLastWeek(@Param("fromDate") fromDate: LocalDateTime): List<News>

    @Query("SELECT n FROM News n JOIN n.entities e WHERE e = :entityName AND n.date >= :fromDate ORDER BY n.date DESC")
    fun findByEntityFromDate(
        @Param("entityName") entityName: String,
        @Param("fromDate") fromDate: LocalDateTime,
    ): List<News>

    @Query("SELECT n FROM News n JOIN n.entities e WHERE e = :entityName AND n.date BETWEEN :fromDate AND :toDate ORDER BY n.date DESC")
    fun findByEntityBetweenDates(
        @Param("entityName") entityName: String,
        @Param("fromDate") fromDate: LocalDateTime,
        @Param("toDate") toDate: LocalDateTime
    ): List<News>

    @Query("""
        SELECT n FROM News n JOIN n.entities e 
        WHERE e = :entityName 
          AND n.date BETWEEN :fromDate AND :toDate 
          AND (:sentiment IS NULL OR n.sentiment = :sentiment)
        ORDER BY n.date DESC
    """)
    fun findByEntityBetweenDatesAndSentiment(
        @Param("entityName") entityName: String,
        @Param("fromDate") fromDate: LocalDateTime,
        @Param("toDate") toDate: LocalDateTime,
        @Param("sentiment") sentiment: String?,
        pageable: Pageable
    ): Page<News>
}