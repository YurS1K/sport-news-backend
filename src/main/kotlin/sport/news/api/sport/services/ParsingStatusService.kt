package sport.news.api.sport.services

import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicBoolean

@Service
class ParsingStatusService {
    private val isParsing = AtomicBoolean(false)

    fun startParsing() {
        isParsing.set(true)
    }

    fun finishParsing() {
        isParsing.set(false)
    }

    fun isParsingInProgress(): Boolean = isParsing.get()
}