package sport.news.api.sport.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File

@Service
class PythonScriptService {
    @Value("\${python.path:python3}")
    private lateinit var pythonPath: String

    private val logger = LoggerFactory.getLogger(javaClass)

    fun runParserScript(scriptPath: String): Boolean {
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
