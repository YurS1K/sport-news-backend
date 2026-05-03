package sport.news.api.sport.services

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import sport.news.api.sport.listeners.NewsDataUpdater
import java.util.concurrent.CompletableFuture

@Service
class ParsingService(
    private val pythonScriptService: PythonScriptService,
    @Lazy private val newsDataUpdater: NewsDataUpdater,  // Добавлен @Lazy
    private val parsingStatusService: ParsingStatusService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Async
    fun runParsingAsync(scriptPath: String): CompletableFuture<Boolean> {
        if (parsingStatusService.isParsingInProgress()) {
            logger.warn("Парсинг уже выполняется")
            return CompletableFuture.completedFuture(false)
        }

        parsingStatusService.startParsing()
        logger.info("Запуск асинхронного парсинга...")

        return try {
            val result = pythonScriptService.runParserScript(scriptPath)
            if (result) {
                newsDataUpdater.loadNews()
            }
            parsingStatusService.finishParsing()
            CompletableFuture.completedFuture(result)
        } catch (e: Exception) {
            logger.error("Ошибка при парсинге", e)
            parsingStatusService.finishParsing()
            CompletableFuture.completedFuture(false)
        }
    }
}