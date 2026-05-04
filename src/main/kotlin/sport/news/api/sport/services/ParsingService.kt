package sport.news.api.sport.services

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import sport.news.api.sport.listeners.NewsDataUpdater
import java.util.concurrent.CompletableFuture

@Service
class ParsingService(
    private val pythonScriptService: PythonScriptService,
    @Lazy private val newsDataUpdater: NewsDataUpdater,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun runParsingAsync(scriptPath: String): CompletableFuture<Boolean> {
        logger.info("Запуск асинхронного парсинга для: $scriptPath")

        return try {
            val result = pythonScriptService.runParserScript(scriptPath)
            if (result) {
                newsDataUpdater.loadNews()
            }
            CompletableFuture.completedFuture(result)
        } catch (e: Exception) {
            logger.error("Ошибка при парсинге $scriptPath", e)
            CompletableFuture.completedFuture(false)
        }
    }
}
