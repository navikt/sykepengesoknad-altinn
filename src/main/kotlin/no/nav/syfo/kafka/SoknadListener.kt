package no.nav.syfo.kafka

import no.nav.syfo.SendTilAltinnService
import no.nav.syfo.kafka.felles.ArbeidssituasjonDTO.ARBEIDSTAKER
import no.nav.syfo.kafka.felles.SoknadsstatusDTO
import no.nav.syfo.kafka.felles.SoknadsstatusDTO.SENDT
import no.nav.syfo.kafka.felles.SoknadstypeDTO.BEHANDLINGSDAGER
import no.nav.syfo.kafka.felles.SoknadstypeDTO.ARBEIDSTAKERE

import no.nav.syfo.kafka.interfaces.Soknad
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import no.nav.syfo.log
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.LocalDateTime.now
import javax.inject.Inject

@Component
class SoknadListener @Inject
constructor(private val sendTilAltinnService: SendTilAltinnService,
            private val rebehandleSykepengesoknadProducer: RebehandleSykepengesoknadProducer) {
    val log = log()

    @KafkaListener(topics = ["syfo-soknad-v2", "syfo-soknad-v3"], id = "soknadSendt", idIsGroup = false, containerFactory = "kafkaListenerContainerFactory")
    fun listen(cr: ConsumerRecord<String, Soknad>, acknowledgment: Acknowledgment) {
        try {
            MDC.put(NAV_CALLID, getSafeNavCallIdHeaderAsString(cr.headers()))

            val sykepengesoknadDTO = cr.value() as SykepengesoknadDTO

            if ((sykepengesoknadDTO.type == ARBEIDSTAKERE
                            || (sykepengesoknadDTO.type == BEHANDLINGSDAGER && sykepengesoknadDTO.arbeidssituasjon == ARBEIDSTAKER))
                    && sykepengesoknadDTO.status == SENDT
                    && sykepengesoknadDTO.sendtArbeidsgiver != null) {
                val sykepengesoknad = konverter(sykepengesoknadDTO)

                try {
                    sendTilAltinnService.sendSykepengesoknadTilAltinn(sykepengesoknad)
                } catch (e: Exception) {
                    log.error("Feiler ved sending av søknad ${sykepengesoknadDTO.id}, legger til rebehandling", e)
                    rebehandleSykepengesoknadProducer.send(sykepengesoknad, now().plusMinutes(1))
                }
            } else {
                log.info("Ignorerer søknad ${sykepengesoknadDTO.id} med status ${sykepengesoknadDTO.status} og type ${sykepengesoknadDTO.type}")
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
