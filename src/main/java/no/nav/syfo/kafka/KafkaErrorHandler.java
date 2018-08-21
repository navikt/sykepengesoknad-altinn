package no.nav.syfo.kafka;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.TopicAuthorizationException;
import org.springframework.kafka.listener.ContainerAwareErrorHandler;
import org.springframework.kafka.listener.ContainerStoppingErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class KafkaErrorHandler implements ContainerAwareErrorHandler {

    private static ContainerStoppingErrorHandler STOPPING_ERROR_HANDLER = new ContainerStoppingErrorHandler();

    private MeterRegistry registry;

    public KafkaErrorHandler(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void handle(Exception thrownException, List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer, MessageListenerContainer container) {
        log.error("Feil i listener:", thrownException);

        /*
            Dette er en hack for å redde Kafka fra en evig løkke om koblingen mot AD går ned. Da vil vi inkrementere en feilmetrikk
            som etter en del gjentakelser vil føre til at isAlive flippes og poden blir restartet av kubernetes.
         */
        if (exceptionIsClass(thrownException, TopicAuthorizationException.class)) {
            log.error("Kafka infrastrukturfeil. TopicAuthorizationException ved lesing av topic");
            registry.counter("$APPLIKASJONSNAVN$.kafka.feil", Tags.of("type", "fatale")).increment();
            return;
        }

        records
                .forEach(record -> log.error("Feil i prossesseringen av record med offset:{} og innhold:{}", record.offset(), record.value()));

        STOPPING_ERROR_HANDLER.handle(thrownException, records, consumer, container);
    }

    private boolean exceptionIsClass(Throwable t, Class klazz) {
        int maxdepth = 10;
        while (maxdepth-- > 0 && t != null && !klazz.isInstance(t)) {
            t = t.getCause();
        }

        return klazz.isInstance(t);
    }
}
