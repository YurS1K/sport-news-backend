package sport.news.api.sport.listeners

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import sport.news.api.sport.entities.News
import sport.news.api.sport.repositories.NewsRepository
import sport.news.api.sport.services.AuthorStatsCacheService
import sport.news.api.sport.services.NewsCsvService
import sport.news.api.sport.services.ParsingService
import sport.news.api.sport.services.TopEntitiesCacheService
import java.nio.file.Paths

@Component
class NewsDataUpdater(
    private val parsingService: ParsingService,
    private val csvService: NewsCsvService,
    private val newsRepository: NewsRepository,
    private val topEntitiesCacheService: TopEntitiesCacheService,
    private val authorStatsCacheService: AuthorStatsCacheService,
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val projectRoot = Paths.get("").toAbsolutePath().toString()
    private val scriptRiaPath = "$projectRoot/src/main/kotlin/sport/news/api/sport/scripts/ria_parsing.py"
    private val scriptChampionatPath = "$projectRoot/src/main/kotlin/sport/news/api/sport/scripts/championat_parsing.py"

    @PostConstruct
    fun updateNewsDataOnStartup() {
        println("Запуск начальной загрузки данных в фоновом режиме...")
        scope.launch { parsingService.runParsingAsync(scriptRiaPath) }
        scope.launch { parsingService.runParsingAsync(scriptChampionatPath) }
    }

    @Scheduled(cron = "0 0 */3 * * ?")
    fun updateChampionatNewsDataScheduled() {
        println("Плановое обновление данных Чемпионат...")

        scope.launch { parsingService.runParsingAsync(scriptChampionatPath) }
    }

    @Scheduled(cron = "0 */30 * * * ?")
    fun updateRiaNewsDataScheduled() {
        println("Плановое обновление данных РИА Новости...")

        scope.launch { parsingService.runParsingAsync(scriptRiaPath) }
    }

    @Synchronized
    fun loadNews() {
        val riaNewsList =
            csvService.readNewsFromRiaCsv()
                .distinctBy { it.link }

        val championatNewsList =
            csvService.readNewsFromChampionatCsv()
                .distinctBy { it.link }

        val newNews = mutableListOf<News>()

        for (news in riaNewsList) {
            if (newsRepository.findFirstByLink(news.link) == null) {
                newNews.add(news)
            } else {
                println("Пропущен дубликат: ${news.title}")
            }
        }

        for (news in championatNewsList) {
            if (newsRepository.findFirstByLink(news.link) == null) {
                newNews.add(news)
            } else {
                println("Пропущен дубликат: ${news.title}")
            }
        }

        if (newNews.isNotEmpty()) {
            newsRepository.saveAll(newNews)
            println("Добавлено новых новостей: ${newNews.size}")

            scope.launch { topEntitiesCacheService.refreshTopEntitiesCache() }
            scope.launch { authorStatsCacheService.refreshCache() }
        } else {
            println("Новых новостей нет")
        }
    }
}
