package no.nav.syfo.kafka

import no.nav.syfo.CALL_ID
import no.nav.syfo.kafka.KafkaHeaderConstants.getLastHeaderByKeyAsString
import no.nav.syfo.log
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.util.UUID.randomUUID

@Component
class Listener {
    val log = log()

    @KafkaListener(topics = ["\$topicName$"], id = "\$topicId$", idIsGroup = false)
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        log.debug("Melding mottatt på topic: {} med offsett: {}", cr.topic(), cr.offset())

        try {
            MDC.put(CALL_ID, getLastHeaderByKeyAsString(cr.headers(), CALL_ID, randomUUID().toString()))

            cr.value()

            acknowledgment.acknowledge()
        } catch (e: Exception) {
            log.error("Uventet feil ved behandling av søknad", e)
            throw RuntimeException("Uventet feil ved behandling av søknad")
        } finally {
            MDC.remove(CALL_ID)
        }
    }
}
