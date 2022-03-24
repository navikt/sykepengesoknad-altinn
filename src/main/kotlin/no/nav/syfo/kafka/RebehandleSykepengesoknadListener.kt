package no.nav.syfo.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.syfo.BEHANDLINGSTIDSPUNKT
import no.nav.syfo.SendTilAltinnService
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.logger
import no.nav.syfo.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.event.EventListener
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.event.ConsumerStoppedEvent
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Instant
import java.time.ZoneOffset

const val RETRY_TOPIC = "flex." + "sykepengesoknad-altinn-retry"

@Component
class RebehandleSykepengesoknadListener(
    private val sendTilAltinnService: SendTilAltinnService,
    private val rebehandleSykepengesoknadProducer: RebehandleSykepengesoknadProducer
) {
    val log = logger()

    @KafkaListener(
        topics = [RETRY_TOPIC],
        containerFactory = "aivenKafkaListenerContainerFactory",
        properties = ["auto.offset.reset=earliest"],
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val sykepengesoknad = cr.value().tilSykepengesoknad()
        val behandlingstidspunkt = cr.headers().lastHeader(BEHANDLINGSTIDSPUNKT)
            ?.value()
            ?.let { String(it, UTF_8) }
            ?.let { Instant.ofEpochMilli(it.toLong()) }
            ?: Instant.now()

        if (sykepengesoknad.id in listOf("82e53926-1751-331d-b2d5-668f3df92cb0", "303fc735-7b99-3a18-aaf9-579deca67066",)) {
            log.info("Behandler ikke søknad ${sykepengesoknad.id}")
            acknowledgment.acknowledge()
            return
        }

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
            rebehandleSykepengesoknadProducer.send(sykepengesoknad)
            log.error("Uventet feil ved rebehandling av søknad ${sykepengesoknad.id}, legger søknaden tilbake på kø", e)

            acknowledgment.acknowledge()
        }
    }

    @EventListener
    fun eventHandler(event: ConsumerStoppedEvent) {
        if (event.reason == ConsumerStoppedEvent.Reason.NORMAL) {
            return
        }
        log.error("Consumer stoppet grunnet ${event.reason}, restarter app")
    }

    fun String.tilSykepengesoknad(): Sykepengesoknad = objectMapper.readValue(this)
}
