package no.nav.syfo.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.syfo.SendTilAltinnService
import no.nav.syfo.kafka.felles.ArbeidssituasjonDTO
import no.nav.syfo.kafka.felles.SoknadsstatusDTO
import no.nav.syfo.kafka.felles.SoknadstypeDTO
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import no.nav.syfo.logger
import no.nav.syfo.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.event.EventListener
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.event.ConsumerStoppedEvent
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.LocalDateTime

const val SYKEPENGESOKNAD_TOPIC = "flex." + "sykepengesoknad"

@Component
class AivenSykepengesoknadListener(
    private val sendTilAltinnService: SendTilAltinnService,
    private val rebehandleSykepengesoknadProducer: RebehandleSykepengesoknadProducer,

) {

    private val log = logger()

    @KafkaListener(
        topics = [SYKEPENGESOKNAD_TOPIC],
        containerFactory = "aivenKafkaListenerContainerFactory"
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {

        val sykepengesoknadDTO = cr.value().tilSykepengesoknadDTO()

        if (sykepengesoknadDTO.skalBehandles()) {
            val sykepengesoknad = sykepengesoknadDTO.konverter()
            try {
                sendTilAltinnService.sendSykepengesoknadTilAltinn(sykepengesoknad)
            } catch (e: Exception) {
                log.error("Feiler ved sending av søknad ${sykepengesoknadDTO.id}, legger til rebehandling", e)
                rebehandleSykepengesoknadProducer.send(sykepengesoknad, LocalDateTime.now().plusMinutes(1))
            }
        } else {
            log.info("Ignorerer søknad ${sykepengesoknadDTO.id} med status ${sykepengesoknadDTO.status} og type ${sykepengesoknadDTO.type}")
        }

        acknowledgment.acknowledge()
    }

    @EventListener
    fun eventHandler(event: ConsumerStoppedEvent) {
        if (event.reason == ConsumerStoppedEvent.Reason.NORMAL) {
            return
        }
        log.error("Consumer stoppet grunnet ${event.reason}")
        if (event.source is KafkaMessageListenerContainer<*, *> &&
            event.reason == ConsumerStoppedEvent.Reason.AUTH
        ) {
            val container = event.source as KafkaMessageListenerContainer<*, *>
            log.info("Trying to restart consumer, creds may be rotated")
            container.start()
        }
    }

    fun String.tilSykepengesoknadDTO(): SykepengesoknadDTO = objectMapper.readValue(this)

    fun SykepengesoknadDTO.skalBehandles(): Boolean {
        return (
            this.type == SoknadstypeDTO.ARBEIDSTAKERE ||
                (this.type == SoknadstypeDTO.BEHANDLINGSDAGER && this.arbeidssituasjon == ArbeidssituasjonDTO.ARBEIDSTAKER)
            ) &&
            this.status == SoknadsstatusDTO.SENDT &&
            this.sendtArbeidsgiver != null
    }
}
