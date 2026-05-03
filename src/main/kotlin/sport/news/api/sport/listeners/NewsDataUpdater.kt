package sport.news.api.sport.listeners

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import sport.news.api.sport.entities.News
import sport.news.api.sport.services.ParsingService
import sport.news.api.sport.services.NewsCsvService
import sport.news.api.sport.repositories.NewsRepository
import java.nio.file.Paths

@Component
class NewsDataUpdater(
    private val parsingService: ParsingService,
    private val csvService: NewsCsvService,
    private val newsRepository: NewsRepository
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    @PostConstruct
    fun updateNewsDataOnStartup() {
        val projectRoot = Paths.get("").toAbsolutePath().toString()
        val scriptPath = "$projectRoot/src/main/kotlin/sport/news/api/sport/scripts/main.py"

        println("Запуск начальной загрузки данных в фоновом режиме...")
        scope.launch { parsingService.runParsingAsync(scriptPath)}
    }

    @Scheduled(cron = "0 0 3 * * ?")
    fun updateNewsDataScheduled() {
        val projectRoot = Paths.get("").toAbsolutePath().toString()
        val scriptPath = "$projectRoot/src/main/kotlin/sport/news/api/sport/scripts/main.py"

        println("Плановое обновление данных...")
        scope.launch {parsingService.runParsingAsync(scriptPath)}
    }

    @Synchronized
    fun loadNews() {
        val newsList = csvService.readNewsFromCsv()
            .distinctBy { it.link }

        val newNews = mutableListOf<News>()

        for (news in newsList) {
            if (newsRepository.findFirstByLink(news.link) == null) {
                newNews.add(news)
            } else {
                println("Пропущен дубликат: ${news.title}")
            }
        }

        if (newNews.isNotEmpty()) {
            newsRepository.saveAll(newNews)
            println("Добавлено новых новостей: ${newNews.size}")
        } else {
            println("Новых новостей нет")
        }
    }
}