package no.nav.syfo.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static java.util.UUID.randomUUID;
import static no.nav.syfo.Application.CALL_ID;
import static no.nav.syfo.kafka.KafkaHeaderConstants.getLastHeaderByKeyAsString;

@Component
@Slf4j
public class Listener {
    @KafkaListener(topics = "$topicName$", id = "$topicId$", idIsGroup = false)
    public void listen(ConsumerRecord<String, String> cr, Acknowledgment acknowledgment) {
        log.debug("Melding mottatt på topic: {} med offsett: {}", cr.topic(), cr.offset());

        try {
            MDC.put(CALL_ID, getLastHeaderByKeyAsString(cr.headers(), CALL_ID, randomUUID().toString()));

            cr.value();

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Uventet feil ved behandling av søknad", e);
            throw new RuntimeException("Uventet feil ved behandling av søknad");
        } finally {
            MDC.remove(CALL_ID);
        }
    }
}
