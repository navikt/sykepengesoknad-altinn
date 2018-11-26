package no.nav.syfo.kafka

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.syfo.log
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.errors.TopicAuthorizationException
import org.springframework.kafka.listener.ContainerAwareErrorHandler
import org.springframework.kafka.listener.ContainerStoppingErrorHandler
import org.springframework.kafka.listener.MessageListenerContainer
import org.springframework.stereotype.Component

private val STOPPING_ERROR_HANDLER = ContainerStoppingErrorHandler()

@Component
class KafkaErrorHandler(private val registry: MeterRegistry) : ContainerAwareErrorHandler {
    val log = log()

    override fun handle(
        thrownException: Exception,
        records: List<ConsumerRecord<*, *>>?,
        consumer: Consumer<*, *>?,
        container: MessageListenerContainer?
    ) {
        log.error("Feil i listener:", thrownException)

        /*
            Dette er en hack for å redde Kafka fra en evig løkke om koblingen mot AD går ned. Da vil vi inkrementere en feilmetrikk
            som etter en del gjentakelser vil føre til at isAlive flippes og poden blir restartet av kubernetes.
         */
        if (exceptionIsClass(thrownException, TopicAuthorizationException::class.java)) {
            log.error("Kafka infrastrukturfeil. TopicAuthorizationException ved lesing av topic")
            registry.counter("syfoaltinn.kafka.feil", Tags.of("type", "fatale")).increment()
            return
        }

        records?.forEach { record ->
            log.error(
                "Feil i prossesseringen av record med offset:{} og innhold:{}",
                record.offset(),
                record.value()
            )
        }

        STOPPING_ERROR_HANDLER.handle(thrownException, records, consumer, container!!)
    }

    private fun exceptionIsClass(throwable: Throwable?, klazz: Class<*>): Boolean {
        var t = throwable
        var maxdepth = 10
        while (maxdepth-- > 0 && t != null && !klazz.isInstance(t)) {
            t = t.cause
        }

        return klazz.isInstance(t)
    }
}
