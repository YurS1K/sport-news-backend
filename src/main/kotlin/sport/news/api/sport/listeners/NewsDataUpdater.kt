package sport.news.api.sport.listeners

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import sport.news.api.sport.services.PythonScriptService
import java.nio.file.Paths

@Component
class NewsDataUpdater(
    private val pythonScriptService: PythonScriptService
) {

    @PostConstruct
    fun updateNewsDataOnStartup() {
        val projectRoot = Paths.get("").toAbsolutePath().toString()
        val scriptPath = "$projectRoot\\src\\main\\kotlin\\sport\\news\\api\\sport\\scripts\\main.py"

        println("✅ Обновляем данные новостей...")
//        if (pythonScriptService.runParserScript(scriptPath)) {
//            println("✅ Новые данные успешно сгенерированы")
//        } else {
//            println("❌ Не удалось обновить данные. Используем старый CSV.")
//        }
    }
}