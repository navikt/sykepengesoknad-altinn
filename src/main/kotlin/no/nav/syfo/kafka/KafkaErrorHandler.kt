package no.nav.syfo.kafka

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.syfo.log
import no.nav.syfo.selftest.ApplicationState
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.errors.TopicAuthorizationException
import org.springframework.kafka.listener.ContainerAwareErrorHandler
import org.springframework.kafka.listener.ContainerStoppingErrorHandler
import org.springframework.kafka.listener.MessageListenerContainer
import org.springframework.stereotype.Component

private val STOPPING_ERROR_HANDLER = ContainerStoppingErrorHandler()

@Component
class KafkaErrorHandler(private val registry: MeterRegistry, private val applicationState: ApplicationState) : ContainerAwareErrorHandler {
    val log = log()

    override fun handle(
        thrownException: Exception,
        records: List<ConsumerRecord<*, *>>?,
        consumer: Consumer<*, *>?,
        container: MessageListenerContainer
    ) {
        log.error("Feil i listener:", thrownException)

        if (exceptionIsClass(thrownException, TopicAuthorizationException::class.java)) {
            log.error("Kafka infrastrukturfeil. TopicAuthorizationException ved lesing av topic")
            registry.counter("syfoaltinn.kafka.feil", Tags.of("type", "fatale")).increment()
            log.error("Restarter appen pga TopicAuthorizationException ved lesing av topic")
            applicationState.iAmDead()
            return
        }

        records?.forEach { record ->
            log.error(
                "Feil i prossesseringen av record med offset:{} og innhold:{}",
                record.offset(),
                record.value()
            )
        }

        try {
            registry.counter("syfoaltinn.kafkalytter.stoppet", Tags.of("type", "feil", "help", "Kafkalytteren har stoppet som følge av feil.")).increment()
            STOPPING_ERROR_HANDLER.handle(thrownException, records, consumer, container)
        } finally {
            log.error("Restarter appen pga kafka-feil")
            applicationState.iAmDead()
        }
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
