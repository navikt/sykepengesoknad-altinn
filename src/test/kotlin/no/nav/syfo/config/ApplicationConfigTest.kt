package no.nav.syfo.config

import no.nav.syfo.LocalApplication
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka

@EmbeddedKafka
@SpringBootTest(classes = [LocalApplication::class])
class ApplicationConfigTest {

    @Test
    fun test() {
    }
}
