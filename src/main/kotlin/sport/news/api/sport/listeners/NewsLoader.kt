package sport.news.api.sport.listener

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sport.news.api.sport.entities.News
import sport.news.api.sport.repositories.NewsRepository
import sport.news.api.sport.services.NewsCsvService

@Component
class NewsLoader(
    private val csvService: NewsCsvService,
    private val newsRepository: NewsRepository
) {

    @PostConstruct
    @Transactional
    fun loadNewsOnStartup() {
        val newsList = csvService.readNewsFromCsv()
            .distinctBy { it.link }

        val newNews = mutableListOf<News>()

        for (news in newsList) {
            if (newsRepository.findFirstByLink(news.link) == null) {
                newNews.add(news)
            } else {
                println("Пропущен дубликат: ${news.title} (${news.link})")
            }
        }

        if (newNews.isNotEmpty()) {
            newsRepository.saveAll(newNews)
            println("Добавлено новых новостей: ${newNews.size}")
        } else {
            println("Новых новостей нет — все уже в базе.")
        }
    }
}