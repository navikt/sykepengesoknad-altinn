package no.nav.syfo.kafka

import no.nav.syfo.log
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.Collections.singletonMap
import java.util.UUID.randomUUID

@Component
class TestProducer(private val kafkaTemplate: KafkaTemplate<String, String>) {
    val log = log()

    fun sendMelding() {
        kafkaTemplate.send(
            SyfoProducerRecord(
                "\$topicName$",
                randomUUID().toString(),
                "test data",
                singletonMap<String, Any>("test", "header")
            )
        )
        log.info("Testdata er sendt")
    }
}
