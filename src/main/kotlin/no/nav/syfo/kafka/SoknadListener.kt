package no.nav.syfo.kafka

import no.nav.syfo.SendTilAltinnService
import no.nav.syfo.domain.soknad.Soknadsstatus
import no.nav.syfo.domain.soknad.Soknadstype
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

    @KafkaListener(topics = ["syfo-soknad-v2"], id = "soknadSendt", idIsGroup = false, containerFactory = "kafkaListenerContainerFactory")
    fun listen(cr: ConsumerRecord<String, Soknad>, acknowledgment: Acknowledgment) {
        try {
            MDC.put(NAV_CALLID, getSafeNavCallIdHeaderAsString(cr.headers()))

            val sykepengesoknad = konverter(cr.value() as SykepengesoknadDTO)

            if (sykepengesoknad.type == Soknadstype.ARBEIDSTAKERE
                    && sykepengesoknad.status == Soknadsstatus.SENDT
                    && sykepengesoknad.sendtArbeidsgiver != null) {
                try {
                    sendTilAltinnService.sendSykepengesoknadTilAltinn(sykepengesoknad)
                } catch (e: Exception) {
                    log.error("Feiler ved sending av søknad ${sykepengesoknad.id}, legger til rebehandling", e)
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
