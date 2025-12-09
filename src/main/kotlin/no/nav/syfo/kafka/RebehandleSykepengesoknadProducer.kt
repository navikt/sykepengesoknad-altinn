package no.nav.syfo.kafka

import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import no.nav.syfo.BEHANDLINGSTIDSPUNKT
import no.nav.syfo.logger
import no.nav.syfo.serialisertTilString
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.Header
import org.apache.kafka.common.header.internals.RecordHeader
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class RebehandleSykepengesoknadProducer(
    private val aivenKafkaConfig: AivenKafkaConfig,
    @param:Value("\${rebehandling.delay.sekunder}") private val delaySekunder: Long,
) {
    val log = logger()
    var producer = aivenKafkaConfig.skapProducer()

    fun send(sykepengesoknadDTO: SykepengesoknadDTO) {
        try {
            producer
                .send(
                    ProducerRecord(
                        RETRY_TOPIC,
                        null,
                        sykepengesoknadDTO.id,
                        sykepengesoknadDTO.serialisertTilString(),
                        ArrayList<Header>().also {
                            it.add(
                                RecordHeader(
                                    BEHANDLINGSTIDSPUNKT,
                                    OffsetDateTime
                                        .now()
                                        .plusSeconds(delaySekunder)
                                        .toInstant()
                                        .toEpochMilli()
                                        .toString()
                                        .toByteArray(),
                                ),
                            )
                        },
                    ),
                ).get()
        } catch (e: Exception) {
            log.error("Det feiler når søknad ${sykepengesoknadDTO.id} skal legges på rebehandle topic", e)
            throw e
        }
    }
}
