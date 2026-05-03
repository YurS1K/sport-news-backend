package sport.news.api.sport

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SportApplication

fun main(args: Array<String>) {
    runApplication<SportApplication>(*args)
}
