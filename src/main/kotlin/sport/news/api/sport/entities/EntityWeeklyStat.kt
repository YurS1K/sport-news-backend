package sport.news.api.sport.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "entity_weekly_stat",
    indexes = [Index(name = "idx_calculated_at", columnList = "calculated_at")],
)
data class EntityWeeklyStat(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val entity: String,
    @Column(nullable = false)
    val count: Long,
    @Column(name = "calculated_at", nullable = false)
    val calculatedAt: LocalDateTime,
)
