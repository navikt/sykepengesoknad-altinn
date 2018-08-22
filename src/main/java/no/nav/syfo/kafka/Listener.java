package no.nav.syfo.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Listener {
    @KafkaListener(topics = "$topicName$", id = "$topicId$", idIsGroup = false)
    public void listen(ConsumerRecord<String, String> cr, Acknowledgment acknowledgment) {
        log.info("Melding mottatt på topic: {} med offsett: {}", cr.topic(), cr.offset());
        acknowledgment.acknowledge();
    }
}
