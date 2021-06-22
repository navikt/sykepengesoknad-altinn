package no.nav.syfo.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.syfo.BEHANDLINGSTIDSPUNKT
import no.nav.syfo.SendTilAltinnService
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.logger
import no.nav.syfo.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Instant
import java.time.ZoneOffset
import javax.inject.Inject

const val RETRY_TOPIC = "flex." + "syfoaltinn-retry"

@Component
class RebehandleSykepengesoknadListener @Inject
constructor(
    private val sendTilAltinnService: SendTilAltinnService,
    private val rebehandleSykepengesoknadProducer: RebehandleSykepengesoknadProducer
) {
    val log = logger()

    @KafkaListener(
        topics = [RETRY_TOPIC],
        containerFactory = "aivenKafkaListenerContainerFactory"
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val sykepengesoknad = cr.value().tilSykepengesoknad()
        val behandlingstidspunkt = cr.headers().lastHeader(BEHANDLINGSTIDSPUNKT)
            ?.value()
            ?.let { String(it, UTF_8) }
            ?.let { Instant.ofEpochMilli(it.toLong()) }
            ?: Instant.now()

        try {
            val sovetid = behandlingstidspunkt.toEpochMilli() - Instant.now().toEpochMilli()
            if (sovetid > 0) {
                log.info(
                    "Mottok rebehandling av søknad ${sykepengesoknad.id} med behandlingstidspunkt ${
                    behandlingstidspunkt.atOffset(
                        ZoneOffset.UTC
                    )
                    } sover i $sovetid millisekunder"
                )
                acknowledgment.nack(sovetid)
            } else {
                sendTilAltinnService.sendSykepengesoknadTilAltinn(sykepengesoknad)
                acknowledgment.acknowledge()
            }
        } catch (e: Exception) {
            if (sykepengesoknad.id == "0edcaa74-257f-4c13-81db-e21b8cb2e8c8") {
                log.info("Skipper ${sykepengesoknad.id} fra rebehandling siden den er veldig gammel")
                acknowledgment.acknowledge()
            }
            rebehandleSykepengesoknadProducer.send(sykepengesoknad)
            log.error("Uventet feil ved rebehandling av søknad ${sykepengesoknad.id}, legger søknaden tilbake på kø", e)

            acknowledgment.acknowledge()
        }
    }

    fun String.tilSykepengesoknad(): Sykepengesoknad = objectMapper.readValue(this)
}
