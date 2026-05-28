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
}
