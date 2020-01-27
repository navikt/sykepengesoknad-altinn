package no.nav.syfo.kafka

import no.nav.syfo.SendTilAltinnService
import no.nav.syfo.domain.Arbeidssituasjon.ARBEIDSTAKER
import no.nav.syfo.domain.soknad.Soknadsstatus.SENDT
import no.nav.syfo.kafka.sykepengesoknadbehandlingsdager.dto.SykepengesoknadBehandlingsdagerDTO
import no.nav.syfo.log
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.LocalDateTime.now
import javax.inject.Inject

@Component
class SoknadBehandlingsdagerListener @Inject
constructor(private val sendTilAltinnService: SendTilAltinnService,
            private val rebehandleSykepengesoknadProducer: RebehandleSykepengesoknadProducer) {
    val log = log()

    @KafkaListener(topics = ["syfo-soknad-behandlingsdager-v1"], id = "soknadBehandlingsdagerSendt", idIsGroup = false, containerFactory = "kafkaListenerContainerFactoryBehandlingsdager")
    fun listen(cr: ConsumerRecord<String, SykepengesoknadBehandlingsdagerDTO>, acknowledgment: Acknowledgment) {
        try {
            MDC.put(NAV_CALLID, getSafeNavCallIdHeaderAsString(cr.headers()))

            val sykepengesoknad = konverter(cr.value() as SykepengesoknadBehandlingsdagerDTO)

            if (sykepengesoknad.arbeidssituasjon == ARBEIDSTAKER
                    && sykepengesoknad.status == SENDT
                    && sykepengesoknad.sendtArbeidsgiver != null) {
                try {
                    sendTilAltinnService.sendSykepengesoknadTilAltinn(sykepengesoknad)
                    log.info("Søknad ${sykepengesoknad.id} er sendt til Altinn")
                } catch (e: Exception) {
                    log.error("Feiler ved sending av behandlingsdager søknad ${sykepengesoknad.id}, legger til rebehandling", e)
                    rebehandleSykepengesoknadProducer.send(sykepengesoknad, now().plusMinutes(1))
                }
            }

            acknowledgment.acknowledge()
        } catch (e: Exception) {
            log.error("Uventet feil ved mottak av søknad på topic: ${cr.topic()}", e)
            throw RuntimeException("Uventet feil ved mottak av søknad på topic: ${cr.topic()}")
        } finally {
            MDC.remove(NAV_CALLID)
        }
    }
}
