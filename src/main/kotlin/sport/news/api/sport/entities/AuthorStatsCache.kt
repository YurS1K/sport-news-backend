package sport.news.api.sport.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "author_stats_cache",
    indexes = [Index(name = "idx_calculated_at", columnList = "calculated_at")],
)
data class AuthorStatsCache(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true)
    val author: String,
    @Column(nullable = false)
    val totalNews: Int,
    @Column(nullable = false)
    val avgSentiment: Double,
    @Column(columnDefinition = "TEXT", nullable = false)
    val topEntitiesJson: String,
    @Column(columnDefinition = "TEXT", nullable = false)
    val sourceDistributionJson: String,
    @Column(name = "calculated_at", nullable = false)
    val calculatedAt: LocalDateTime,
)
