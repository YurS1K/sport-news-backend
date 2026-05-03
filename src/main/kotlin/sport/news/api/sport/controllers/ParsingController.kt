package sport.news.api.sport.controllers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.web.bind.annotation.*
import sport.news.api.sport.services.ParsingService
import sport.news.api.sport.services.ParsingStatusService
import java.nio.file.Paths

@RestController
@RequestMapping("/parsing")
class ParsingController(
    private val parsingService: ParsingService,
    private val parsingStatusService: ParsingStatusService
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @PostMapping("/run")
    fun runParsing(): Map<String, String> {
        if (parsingStatusService.isParsingInProgress()) {
            return mapOf(
                "status" to "BUSY",
                "message" to "Парсинг уже выполняется. Пожалуйста, подождите."
            )
        }

        val projectRoot = Paths.get("").toAbsolutePath().toString()
        val scriptPath = "$projectRoot/src/main/kotlin/sport/news/api/sport/scripts/main.py"

        scope.launch { parsingService.runParsingAsync(scriptPath) }
        return mapOf(
            "status" to "STARTED",
            "message" to "Парсинг запущен в фоновом режиме"
        )
    }

    @GetMapping("/status")
    fun getParsingStatus(): Map<String, Any> {
        return mapOf(
            "isParsing" to parsingStatusService.isParsingInProgress(),
            "timestamp" to System.currentTimeMillis()
        )
    }
}