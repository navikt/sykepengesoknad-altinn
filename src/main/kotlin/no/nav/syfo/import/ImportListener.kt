package no.nav.syfo.import

import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.core.instrument.MeterRegistry
import no.nav.syfo.domain.SendtSoknad
import no.nav.syfo.logger
import no.nav.syfo.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import kotlin.system.measureTimeMillis

const val MIGRERING_TOPIC = "flex." + "syfoaltinn-migrering"

@Component
class ImportListener(
    private val innsendingRepository: BatchInsertDAO,
    registry: MeterRegistry,
) {

    private val log = logger()
    val counter = registry.counter("mottatt_migrering_counter")

    @KafkaListener(
        topics = [MIGRERING_TOPIC],
        containerFactory = "sendtSykmeldingContainerFactory",
    )
    fun listen(records: List<ConsumerRecord<String, String>>, acknowledgment: Acknowledgment) {

        val raderFraKafka = records
            .map { it.value().tilInnsendingKafkaDto() }
            .distinctBy { it.sykepengesoknadId }
            .map { it.tilInnsendingDbRecord() }

        if (raderFraKafka.isEmpty()) {
            acknowledgment.acknowledge()
            return
        }

        val elapsed = measureTimeMillis {
            innsendingRepository.batchInsertInnsending(raderFraKafka)
            counter.increment(raderFraKafka.size.toDouble())
        }
        log.info("Behandlet ${raderFraKafka.size} innsending rader fra kafka i $elapsed millis")

        acknowledgment.acknowledge()
    }
}

fun String.tilInnsendingKafkaDto(): SendtSoknadKafkaDto = objectMapper.readValue(this)

fun SendtSoknadKafkaDto.tilInnsendingDbRecord(): SendtSoknad {
    return SendtSoknad(
        id = null,
        sykepengesoknadId = sykepengesoknadId,
        sendt = sendt.toInstant(),
    )
}

data class SendtSoknadKafkaDto(
    val sykepengesoknadId: String,
    val sendt: OffsetDateTime,
)
