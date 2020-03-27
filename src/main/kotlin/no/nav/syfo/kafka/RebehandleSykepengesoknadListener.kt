package no.nav.syfo.kafka

import no.nav.syfo.BEHANDLINGSTIDSPUNKT
import no.nav.syfo.SendTilAltinnService
import no.nav.syfo.domain.soknad.Sykepengesoknad
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
class RebehandleSykepengesoknadListener @Inject
constructor(private val sendTilAltinnService: SendTilAltinnService,
            private val rebehandleSykepengesoknadProducer: RebehandleSykepengesoknadProducer) {
    val log = log()

    @KafkaListener(topics = ["privat-syfoaltinn-soknad-v2"], id = "syfoaltinnIntern-v2", idIsGroup = false, containerFactory = "kafkaListenerContainerFactoryRebehandling")
    fun listen(cr: ConsumerRecord<String, Sykepengesoknad>, acknowledgment: Acknowledgment) {
        try {
            MDC.put(NAV_CALLID, getSafeNavCallIdHeaderAsString(cr.headers()))
            val sykepengesoknad: Sykepengesoknad = cr.value()

            cr.headers().lastHeader(BEHANDLINGSTIDSPUNKT)
                    ?.value()
                    ?.let { String(it, UTF_8) }
                    ?.let { LocalDateTime.parse(it, ISO_LOCAL_DATE_TIME) }
                    ?.takeIf { now().isBefore(it) }
                    ?.apply {
                        log.info("Plukket opp søknad ${cr.key()} med senere behandlingstidspunkt, venter 10 sekunder og legger tilbake på kø...")
                        Thread.sleep(10000)
                        rebehandleSykepengesoknadProducer.send(sykepengesoknad, this)
                        acknowledgment.acknowledge()
                        return
                    }

            sendTilAltinnService.sendSykepengesoknadTilAltinn(sykepengesoknad)
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            val sykepengesoknad: Sykepengesoknad = cr.value()
            if (sykepengesoknad.sykmeldingId == "a66d14cc-5c3a-41be-ab1f-657bc81f4874") {
                log.info("Ignorerer feilsituasjon for sykmelding id ${sykepengesoknad.sykmeldingId}")
            } else {
                rebehandleSykepengesoknadProducer.send(sykepengesoknad, now().plusMinutes(1))
                log.error("Uventet feil ved rebehandling av søknad ${sykepengesoknad.id}, legger søknaden tilbake på kø", e)
            }
            acknowledgment.acknowledge()
        } finally {
            MDC.remove(NAV_CALLID)
        }
    }
}
