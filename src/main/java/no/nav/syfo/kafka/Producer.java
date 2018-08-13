package no.nav.syfo.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static java.util.UUID.randomUUID;

@Component
@Slf4j
public class Producer {
    private KafkaTemplate<String, String> kafkaTemplate;

    public Producer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // TODO: Opprett topic
    public void send() {
        kafkaTemplate.send(
                "topic",
                randomUUID().toString(),
                "data");
        log.info("Data lagt på kø");
    }
}
