package no.nav.syfo.kafka

import no.nav.syfo.BEHANDLINGSTIDSPUNKT
import no.nav.syfo.SendTilAltinnService
import no.nav.syfo.kafka.interfaces.Soknad
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import no.nav.syfo.log
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets.UTF_8
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import javax.inject.Inject

@Component
class InternSoknadsbehandlingListener @Inject
constructor(private val sendTilAltinnService: SendTilAltinnService,
            private val rebehandleSykepengesoknadProducer: RebehandleSykepengesoknadProducer) {
    val log = log()

    @KafkaListener(topics = ["privat-syfoaltinn-soknad-v1"],id = "syfoaltinnIntern", idIsGroup = false, containerFactory = "kafkaListenerContainerFactory")
    fun listen(cr: ConsumerRecord<String, Soknad>, acknowledgment: Acknowledgment) {
        try {
            MDC.put(NAV_CALLID, getSafeNavCallIdHeaderAsString(cr.headers()))
            val sykepengesoknad = konverter(cr.value() as SykepengesoknadDTO)

            cr.headers().lastHeader(BEHANDLINGSTIDSPUNKT)
                    ?.value()
                    ?.let { String(it, UTF_8) }
                    ?.let { LocalDateTime.parse(it, ISO_LOCAL_DATE_TIME) }
                    ?.takeIf { now().isBefore(it) }
                    ?.apply {
                        log.info("Plukket opp søknad ${sykepengesoknad.id} med senere behandlingstidspunkt, venter 10 sekunder og legger tilbake på kø...")
                        Thread.sleep(10000)
                        log.info("Legger søknad på NY rebehandling topic")
                        rebehandleSykepengesoknadProducer.send(sykepengesoknad, this)
                        acknowledgment.acknowledge()
                        return
                    }

            sendTilAltinnService.sendSykepengesoknadTilAltinn(sykepengesoknad)
            log.info("Søknad ${sykepengesoknad.id} er sendt til Altinn")
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            val sykepengesoknad = konverter(cr.value() as SykepengesoknadDTO)
            rebehandleSykepengesoknadProducer.send(sykepengesoknad, now().plusMinutes(1))
            log.error("Uventet feil ved behandling av søknad ${sykepengesoknad.id}, legger søknaden på NY rebehandling topic", e)
            acknowledgment.acknowledge()
        } finally {
            MDC.remove(NAV_CALLID)
        }
    }
}
