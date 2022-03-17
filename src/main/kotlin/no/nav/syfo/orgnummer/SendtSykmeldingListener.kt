package no.nav.syfo.orgnummer

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.syfo.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

const val SYKMELDINGSENDT_TOPIC = "teamsykmelding." + "syfo-sendt-sykmelding"

@Component
class SendtSykmeldingListener(
    val juridiskOrgnummerRepository: BatchInsertOrgnummerDAO
) {

    @KafkaListener(
        topics = [SYKMELDINGSENDT_TOPIC],
        containerFactory = "sendtSykmeldingContainerFactory"
    )
    fun listen(records: List<ConsumerRecord<String, String>>, acknowledgment: Acknowledgment) {
        val rader = records
            .map { it.value().tilSykmeldingKafkaMessage() }
            .map {
                if (it.event.arbeidsgiver == null) {
                    null
                } else {
                    val arbeidsgiver = it.event.arbeidsgiver!!
                    val sykmeldingId = it.event.sykmeldingId
                    JuridiskOrgnummer(
                        id = null,
                        sykmeldingId = sykmeldingId,
                        juridiskOrgnummer = arbeidsgiver.juridiskOrgnummer,
                        orgnummer = arbeidsgiver.orgnummer
                    )
                }
            }.filterNotNull()
        juridiskOrgnummerRepository.batchInsertOrgnummer(rader)
        acknowledgment.acknowledge()
    }
}

fun String.tilSykmeldingKafkaMessage(): SykmeldingKafkaMessage = objectMapper.readValue(this)
