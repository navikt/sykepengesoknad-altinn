package no.nav.syfo.kafka

import no.nav.syfo.kafka.KafkaHeaderConstants.MELDINGSTYPE
import no.nav.syfo.kafka.interfaces.Soknad
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import no.nav.syfo.log
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.Collections.singletonMap
import java.util.UUID.randomUUID

@Component
class TestProducer(private val kafkaTemplate: KafkaTemplate<String, Soknad>) {
    val log = log()

    fun sendMelding() {

        val syfoProducerRecord: SyfoProducerRecord<String, Soknad> = SyfoProducerRecord(
                "privat-syfoaltinn-soknad-v1",
                randomUUID().toString(),
                SykepengesoknadDTO.builder().build(),
                singletonMap<String, Any>(MELDINGSTYPE, "SYKEPENGESOKNAD"))

        kafkaTemplate.send(
                syfoProducerRecord
        )
        log.info("Testdata er sendt")
    }
}
