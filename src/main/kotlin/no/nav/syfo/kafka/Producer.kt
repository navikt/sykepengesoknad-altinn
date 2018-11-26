package no.nav.syfo.kafka

import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import no.nav.syfo.log
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.Collections.singletonMap
import java.util.UUID.randomUUID

@Component
class Producer(private val kafkaTemplate: KafkaTemplate<String, SykepengesoknadDTO>) {
    val log = log()

    fun send() {
        kafkaTemplate.send(
                SyfoProducerRecord(
                        "syfoaltinn-rebehandling-v1",
                        randomUUID().toString(),
                        SykepengesoknadDTO.builder().build(),
                        singletonMap<String, Any>("example", "header")
                )
        )
        log.info("Data lagt på kø")
    }
}
