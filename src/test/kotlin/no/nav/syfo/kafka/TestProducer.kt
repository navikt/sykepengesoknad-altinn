package no.nav.syfo.kafka

import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.log
import no.nav.syfo.mockSykepengesoknad
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.Collections.singletonMap
import java.util.UUID.randomUUID

@Component
class TestProducer(private val kafkaTemplate: KafkaTemplate<String, Sykepengesoknad>) {
    val log = log()

    fun sendMelding() {

        val syfoProducerRecord: SyfoProducerRecord<String, Sykepengesoknad> = SyfoProducerRecord(
                "privat-syfoaltinn-soknad-v1",
                randomUUID().toString(),
                mockSykepengesoknad,
                singletonMap<String, Any>(MELDINGSTYPE, "SYKEPENGESOKNAD"))

        kafkaTemplate.send(
                syfoProducerRecord
        )
        log.info("Testdata er sendt")
    }
}
