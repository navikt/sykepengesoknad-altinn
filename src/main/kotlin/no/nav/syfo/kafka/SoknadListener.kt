package no.nav.syfo.kafka

import no.nav.syfo.kafka.interfaces.Soknad
import no.nav.syfo.kafka.sykepengesoknad.dto.SoknadsstatusDTO
import no.nav.syfo.kafka.sykepengesoknad.dto.SoknadstypeDTO
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
constructor(private val internSoknadsbehandlingProducer: InternSoknadsbehandlingProducer) {
    val log = log()

    @KafkaListener(topics = ["syfo-soknad-v2"], id = "soknadSendt", idIsGroup = false)
    fun listen(cr: ConsumerRecord<String, Soknad>, acknowledgment: Acknowledgment) {
        try {
            MDC.put(NAV_CALLID, getSafeNavCallIdHeaderAsString(cr.headers()))

            val sykepengesoknadDTO = cr.value() as SykepengesoknadDTO

            if (SoknadstypeDTO.ARBEIDSTAKERE == sykepengesoknadDTO.type
                    && SoknadsstatusDTO.SENDT == sykepengesoknadDTO.status
                    && sykepengesoknadDTO.sendtArbeidsgiver != null) {

                internSoknadsbehandlingProducer.leggPaInternTopic(sykepengesoknadDTO, now())
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
