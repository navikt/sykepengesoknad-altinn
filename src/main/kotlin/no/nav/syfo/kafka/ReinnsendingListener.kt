package no.nav.syfo.kafka

import no.nav.syfo.CALL_ID
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
import java.util.UUID.randomUUID
import javax.inject.Inject

@Component
class ReinnsendingListener @Inject
constructor(private val internSoknadsbehandlingProducer: InternSoknadsbehandlingProducer) {
    val log = log()

    @KafkaListener(topics = ["syfoaltinn-reinnsending-v1"], id = "reinnsending", idIsGroup = false)
    fun listen(cr: ConsumerRecord<String, Soknad>, acknowledgment: Acknowledgment) {
        try {
            MDC.put(CALL_ID, getLastHeaderByKeyAsString(cr.headers(), CALL_ID) ?: randomUUID().toString())

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
            MDC.remove(CALL_ID)
        }
    }
}
