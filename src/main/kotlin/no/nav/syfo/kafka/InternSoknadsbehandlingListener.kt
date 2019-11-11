package no.nav.syfo.kafka

import no.nav.syfo.BEHANDLINGSTIDSPUNKT
import no.nav.syfo.CALL_ID
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
import java.util.*
import javax.inject.Inject

@Component
class InternSoknadsbehandlingListener @Inject
constructor(private val sendTilAltinnService: SendTilAltinnService,
            private val internSoknadsbehandlingProducer: InternSoknadsbehandlingProducer) {
    val log = log()

    @KafkaListener(topics = ["privat-syfoaltinn-soknad-v1"], id = "syfoaltinnIntern", idIsGroup = false)
    fun listen(cr: ConsumerRecord<String, Soknad>, acknowledgment: Acknowledgment) {
        try {
            MDC.put(CALL_ID, getLastHeaderByKeyAsString(cr.headers(), CALL_ID) ?: UUID.randomUUID().toString())
            cr.headers().lastHeader(BEHANDLINGSTIDSPUNKT)
                    ?.value()
                    ?.let { String(it, UTF_8) }
                    ?.let { LocalDateTime.parse(it, ISO_LOCAL_DATE_TIME) }
                    ?.takeIf { now().isBefore(it) }
                    ?.apply {
                        log.info("Plukket opp søknad ${cr.key()} med senere behandlingstidspunkt, venter 10 sekunder og legger tilbake på kø...")
                        Thread.sleep(10000)
                        internSoknadsbehandlingProducer.leggPaInternTopic(cr.value() as SykepengesoknadDTO, this)
                        acknowledgment.acknowledge()
                        return
                    }

            val sykepengesoknad = konverter(cr.value() as SykepengesoknadDTO)
            sendTilAltinnService.sendSykepengesoknadTilAltinn(sykepengesoknad)
            log.info("Søknad ${sykepengesoknad.id} er sendt til Altinn")
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            val sykepengesoknadDTO = cr.value() as SykepengesoknadDTO

            log.error("Uventet feil ved behandling av søknad ${sykepengesoknadDTO.id}, legger søknaden tilbake på kø", e)
            internSoknadsbehandlingProducer.leggPaInternTopic(sykepengesoknadDTO, now().plusMinutes(1))
            acknowledgment.acknowledge()
        } finally {
            MDC.remove(CALL_ID)
        }
    }
}
