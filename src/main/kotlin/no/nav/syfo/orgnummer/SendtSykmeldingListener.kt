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
    )
    fun listen(records: List<ConsumerRecord<String, String>>, acknowledgment: Acknowledgment) {
        records
            .map { it.value().tilSykmeldingKafkaMessage() }
            .mapNotNull { it.event.arbeidsgiver }
            .forEach { agStatus ->
                val juridiskOrgnummer = agStatus.juridiskOrgnummer
                if (juridiskOrgnummer != null) {
                    val eksisterende = juridiskOrgnummerRepository.findByOrgnummer(agStatus.orgnummer)
                    if (eksisterende != null) {
                        if (eksisterende.juridiskOrgnummer != juridiskOrgnummer) {
                            log.info("Mismatch mellom eksisterende juridisk orgnummer ${eksisterende.juridiskOrgnummer} og nytt $juridiskOrgnummer for ${agStatus.orgnummer}. Oppdater db")
                            juridiskOrgnummerRepository.save(eksisterende.copy(juridiskOrgnummer = juridiskOrgnummer))
                        }
                    } else {
                        juridiskOrgnummerRepository.save(
                            JuridiskOrgnummer(
                                id = null,
                                juridiskOrgnummer = juridiskOrgnummer,
                                orgnummer = agStatus.orgnummer
                            )
                        )
                    }
                }
            }

        acknowledgment.acknowledge()
    }
}

fun String.tilSykmeldingKafkaMessage(): SykmeldingKafkaMessage = objectMapper.readValue(this)
