package no.nav.syfo.kafka

import no.nav.syfo.CALL_ID
import no.nav.syfo.kafka.KafkaHeaderConstants.getLastHeaderByKeyAsString
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import no.nav.syfo.log
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.util.UUID.randomUUID

@Component
class SoknadListener {
    val log = log()

    @KafkaListener(topics = ["syfo-soknad-v1"], id = "soknadSendt", idIsGroup = false)
    fun listen(cr: ConsumerRecord<String, SykepengesoknadDTO>, acknowledgment: Acknowledgment) {
        log.info("Melding mottatt på topic: {} med offsett: {}", cr.topic(), cr.offset())

        try {
            MDC.put(CALL_ID, getLastHeaderByKeyAsString(cr.headers(), CALL_ID, randomUUID().toString()))

            val sykepengesoknad = konverter(cr.value())

            //TODO behandle innsendt søknad
            log.info("har plukket opp søknad: {}", sykepengesoknad.toString())

            acknowledgment.acknowledge()
        } catch (e: Exception) {
            log.error("Uventet feil ved behandling av søknad", e)
            throw RuntimeException("Uventet feil ved behandling av søknad")
        } finally {
            MDC.remove(CALL_ID)
        }
    }
}
