package no.nav.syfo.kafka

import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import no.nav.syfo.log
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.Collections.singletonMap

@Component
class InnsendingFeiletProducer(private val kafkaTemplate: KafkaTemplate<String, SykepengesoknadDTO>) {
    val log = log()

    fun innsendingFeilet(sykepengesoknadDTO: SykepengesoknadDTO) {
        kafkaTemplate.send(
                SyfoProducerRecord(
                        "syfo-altinn-innsending-feilet-v1",
                        sykepengesoknadDTO.id,
                        sykepengesoknadDTO,
                        singletonMap<String, Any>("example", "header")
                )
        )
        log.info("Data lagt på kø")
    }
}
