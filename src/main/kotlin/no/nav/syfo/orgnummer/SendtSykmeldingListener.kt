package no.nav.syfo.orgnummer

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.syfo.logger
import no.nav.syfo.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

const val SYKMELDINGSENDT_TOPIC = "teamsykmelding." + "syfo-sendt-sykmelding"

@Component
class SendtSykmeldingListener(
    val juridiskOrgnummerRepository: JuridiskOrgnummerRepository
) {

    private val log = logger()

    @KafkaListener(
        topics = [SYKMELDINGSENDT_TOPIC],
        containerFactory = "sendtSykmeldingContainerFactory",
        concurrency = "3",
    )
    fun listen(records: List<ConsumerRecord<String, String?>>, acknowledgment: Acknowledgment) {
        records
            .mapNotNull { it.value()?.tilSykmeldingKafkaMessage() }
            .forEach {
                if (it.event.arbeidsgiver == null) {
                    return@forEach
                }

                val arbeidsgiver = it.event.arbeidsgiver!!
                val sykmeldingId = it.event.sykmeldingId
                val eksisterende = juridiskOrgnummerRepository.findBySykmeldingId(sykmeldingId)
                if (eksisterende != null) {
                    if (eksisterende.juridiskOrgnummer != arbeidsgiver.juridiskOrgnummer) {
                        log.warn("Mismatch mellom eksisterende juridisk orgnummer ${eksisterende.juridiskOrgnummer} og nytt ${arbeidsgiver.juridiskOrgnummer} for sykmelding $sykmeldingId")
                    }
                    if (eksisterende.orgnummer != arbeidsgiver.orgnummer) {
                        log.warn("Mismatch mellom eksisterende orgnummer ${eksisterende.orgnummer} og nytt ${arbeidsgiver.orgnummer} for sykmelding $sykmeldingId")
                    }
                } else {
                    juridiskOrgnummerRepository.save(
                        JuridiskOrgnummer(
                            id = null,
                            sykmeldingId = sykmeldingId,
                            juridiskOrgnummer = arbeidsgiver.juridiskOrgnummer,
                            orgnummer = arbeidsgiver.orgnummer
                        )
                    )
                }
            }

        acknowledgment.acknowledge()
    }
}

fun String.tilSykmeldingKafkaMessage(): SykmeldingKafkaMessage = objectMapper.readValue(this)
