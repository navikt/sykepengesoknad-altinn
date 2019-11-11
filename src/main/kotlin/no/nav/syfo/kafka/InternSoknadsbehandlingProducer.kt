package no.nav.syfo.kafka

import no.nav.syfo.BEHANDLINGSTIDSPUNKT
import no.nav.syfo.kafka.interfaces.Soknad
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import no.nav.syfo.log
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import javax.inject.Inject

@Component
class InternSoknadsbehandlingProducer @Inject
constructor(private val kafkaTemplate: KafkaTemplate<String, Soknad>) {
    val log = log()

    fun leggPaInternTopic(sykepengesoknadDTO: SykepengesoknadDTO, behandlingstidspunkt: LocalDateTime) {
        try {
            kafkaTemplate.send(
                    SyfoProducerRecord<String, Soknad>("privat-syfoaltinn-soknad-v1", sykepengesoknadDTO.id!!, sykepengesoknadDTO,
                            mapOf(Pair(MELDINGSTYPE, "SYKEPENGESOKNAD"),
                                    Pair(BEHANDLINGSTIDSPUNKT, behandlingstidspunkt.format(ISO_LOCAL_DATE_TIME))))).get()
        } catch (exception: Exception) {
            log.error("Det feiler når søknad ${sykepengesoknadDTO.id} skal legges på intern topic", exception)
            throw RuntimeException(exception)
        }
    }

}