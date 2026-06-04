package sport.news.api.sport.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import sport.news.api.sport.listeners.NewsDataUpdater
import java.io.File
import java.util.concurrent.CompletableFuture

@Service
class ParsingService(
    @Lazy private val newsDataUpdater: NewsDataUpdater,
) {
    @Value("\${python.path:python3}")
    private lateinit var pythonPath: String

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun runParsingAsync(scriptPath: String): CompletableFuture<Boolean> {
        logger.info("Запуск асинхронного парсинга для: $scriptPath")

        return try {
            val result = runScript(scriptPath)
            if (result) {
                newsDataUpdater.loadNews()
            }
            CompletableFuture.completedFuture(result)
        } catch (e: Exception) {
            logger.error("Ошибка при парсинге $scriptPath", e)
            CompletableFuture.completedFuture(false)
        }
    }

    fun runScript(scriptPath: String): Boolean {
        if (!File(scriptPath).exists()) {
            logger.error("Python-скрипт не найден: $scriptPath")
            return false
        }

        return try {
            val processBuilder = ProcessBuilder(pythonPath, scriptPath)
            processBuilder.directory(File(scriptPath).parentFile)
            processBuilder.redirectErrorStream(true)

            logger.info("Запуск Python-скрипта: $scriptPath")
            val process = processBuilder.start()

            process.inputStream.bufferedReader().use { reader ->
                reader.forEachLine { line ->
                    logger.info("[Python] $line")
                }
            }

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                logger.info("Python-скрипт успешно выполнен")
                true
            } else {
                logger.error("Python-скрипт завершился с кодом: $exitCode")
                false
            }
        } catch (e: Exception) {
            logger.error("Ошибка при запуске Python-скрипта", e)
            false
        }
    }
}
