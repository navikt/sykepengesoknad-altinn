package no.nav.syfo.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class Listener {
    // TODO: Opprett topic
    @KafkaListener(topics = "topic")
    public void listen(ConsumerRecord<String, String> cr) {
        log.info("Mottatt melding med timestamp {} partition {}, offset {}, id {} og value {}",
                toLocalDateTime(cr.timestamp()).format(DateTimeFormatter.ISO_DATE_TIME),
                cr.partition(),
                cr.offset(),
                cr.key(),
                cr.value());
    }

    private LocalDateTime toLocalDateTime(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
