package no.nav.syfo.kafka

import no.nav.syfo.log
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.Collections.singletonMap
import java.util.UUID.randomUUID

@Component
class Producer(private val kafkaTemplate: KafkaTemplate<String, String>) {
    val log = log()

    fun send() {
        kafkaTemplate.send(
            SyfoProducerRecord(
                "\$topicName$",
                randomUUID().toString(),
                "data",
                singletonMap<String, Any>("example", "header")
            )
        )
        log.info("Data lagt på kø")
    }
}
