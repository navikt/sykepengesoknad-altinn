package no.nav.syfo.kafka

import no.nav.syfo.logger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.ConsumerSeekAware
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneOffset

@Component
class DTOListener(
    private val aivenSykepengesoknadListener: AivenSykepengesoknadListener
) : ConsumerSeekAware {

    private val log = logger()
    private val soknadIder = listOf(
        "ae41945a-7558-3ac5-bf9f-ef9aa945c1fd",
        "86f17513-f341-3dd0-8f49-f28305a43ba3",
        "6d10f89c-ef88-3fa1-a8a2-490940951cf6",
        "26945bb5-0414-308b-b634-f33f0d0858a0",
        "5839c930-904f-344d-a641-82e53dab86bb"
    )

    @KafkaListener(
        topics = [SYKEPENGESOKNAD_TOPIC],
        concurrency = "3",
        containerFactory = "aivenKafkaListenerContainerFactory",
        id = "sykepengesoknad-altinn-dto-fix",
        idIsGroup = true,
        properties = ["auto.offset.reset=earliest"]
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        log.info("Soknad ${cr.key()}")

        if (cr.key() in soknadIder) {
            log.info("Mottok soknad ${cr.key()} med på kafka")
            aivenSykepengesoknadListener.listen(cr, acknowledgment)
        }

        if (cr.offset() % 1000 == 0L) {
            acknowledgment.acknowledge()
        }
    }

    override fun onPartitionsAssigned(
        assignments: MutableMap<TopicPartition, Long>,
        callback: ConsumerSeekAware.ConsumerSeekCallback
    ) {
        val startDateInEpochMilli = LocalDate.of(2023, 11, 28).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        callback.seekToTimestamp(assignments.map { it.key }, startDateInEpochMilli)
    }
}
