package no.nav.syfo.kafka

import no.nav.syfo.CALL_ID
import no.nav.syfo.kafka.KafkaHeaderConstants.getLastHeaderByKeyAsString
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
class SoknadListener @Inject
constructor(private val internSoknadsbehandlingProducer: InternSoknadsbehandlingProducer) {
    val log = log()

    @KafkaListener(topics = ["syfo-soknad-v2"], id = "soknadSendt", idIsGroup = false)
    fun listen(cr: ConsumerRecord<String, Soknad>, acknowledgment: Acknowledgment) {
        log.info("Melding mottatt på topic: ${cr.topic()} med offset: ${cr.offset()}")

        try {
            MDC.put(CALL_ID, getLastHeaderByKeyAsString(cr.headers(), CALL_ID).orElse(randomUUID().toString()))

            val sykepengesoknadDTO = cr.value() as SykepengesoknadDTO

            //TODO sjekk at søknaden faktisk skal sendes til arbeidsgiver, ikke bare NAV (sendtArbeidsgiver er satt)
            if (SoknadstypeDTO.ARBEIDSTAKERE == sykepengesoknadDTO.type
                    && SoknadsstatusDTO.SENDT == sykepengesoknadDTO.status) {

                log.info("har plukket opp søknad: ${sykepengesoknadDTO.id}, legger på intern topic")
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
