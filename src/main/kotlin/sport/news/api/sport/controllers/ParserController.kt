package sport.news.api.sport.controllers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sport.news.api.sport.services.ParsingService
import java.nio.file.Paths

@RestController
@RequestMapping("/parser")
class ParserController(
    private val parsingService: ParsingService,
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val projectRoot = Paths.get("").toAbsolutePath().toString()
    private val scriptRiaPath = "$projectRoot/src/main/kotlin/sport/news/api/sport/scripts/ria_parsing.py"
    private val scriptChampionatPath = "$projectRoot/src/main/kotlin/sport/news/api/sport/scripts/championat_parsing.py"

    @GetMapping("/run/ria")
    suspend fun runRiaParsing() {
        scope.launch { parsingService.runParsingAsync(scriptRiaPath) }
    }

    @GetMapping("/run/championat")
    suspend fun runChampionatParsing() {
        scope.launch { parsingService.runParsingAsync(scriptChampionatPath) }
    }

    @GetMapping("/run/all")
    suspend fun runAllParsing() {
        scope.launch { parsingService.runParsingAsync(scriptRiaPath) }
        scope.launch { parsingService.runParsingAsync(scriptChampionatPath) }
    }
}
