package no.nav.syfo.kafka

import no.nav.syfo.BEHANDLINGSTIDSPUNKT
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.log
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import javax.inject.Inject

@Component
class RebehandleSykepengesoknadProducer @Inject
constructor(private val kafkaTemplate: KafkaTemplate<String, Sykepengesoknad>) {
    val log = log()

    fun send(sykepengesoknad: Sykepengesoknad) {
        try {
            kafkaTemplate.send(
                    SyfoProducerRecord<String, Sykepengesoknad>("privat-syfoaltinn-soknad-v2", sykepengesoknad.id, sykepengesoknad,
                            mapOf(Pair(BEHANDLINGSTIDSPUNKT, now().plusMinutes(1).format(ISO_LOCAL_DATE_TIME))))).get()
        } catch (exception: Exception) {
            log.error("Det feiler når søknad ${sykepengesoknad.id} skal legges på rebehandle topic", exception)
            throw RuntimeException(exception)
        }
    }
}
