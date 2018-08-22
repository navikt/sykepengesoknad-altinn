package no.nav.syfo.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static java.util.UUID.randomUUID;

@Component
@Slf4j
public class TestProducer {
    private KafkaTemplate<String, String> kafkaTemplate;

    public TestProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMelding() {
        kafkaTemplate.send(
                "$topicName$",
                randomUUID().toString(),
                "test data");
        log.info("Testdata er sendt");
    }
}
