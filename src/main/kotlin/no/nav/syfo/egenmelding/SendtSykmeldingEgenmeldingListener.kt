package no.nav.syfo.egenmelding

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.syfo.domain.SykmeldingKafkaMessage
import no.nav.syfo.logger
import no.nav.syfo.model.sykmeldingstatus.ShortNameDTO
import no.nav.syfo.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

const val SYKMELDINGSENDT_TOPIC = "teamsykmelding." + "syfo-sendt-sykmelding"

@Component
class SendtSykmeldingEgenmeldingListener(
    val egenmeldingFraSykmeldingRepository: EgenmeldingFraSykmeldingRepository
) {

    private val log = logger()

    @KafkaListener(
        topics = [SYKMELDINGSENDT_TOPIC],
        containerFactory = "sendtSykmeldingEgenmeldingContainerFactory",
        concurrency = "3",
        properties = ["offset.reset=latest"],
        id = "sykmelding-sendt-egenmelding",
        idIsGroup = true
    )
    fun listen(cr: ConsumerRecord<String, String?>, acknowledgment: Acknowledgment) {
        val sykmeldingKafkaMessage = cr.value()?.tilSykmeldingKafkaMessage() ?: run {
            acknowledgment.acknowledge()
            return
        }

        val sykmeldingId = sykmeldingKafkaMessage.event.sykmeldingId

        if (sykmeldingKafkaMessage.event.erSvarOppdatering == true) {
            egenmeldingFraSykmeldingRepository.findBySykmeldingId(sykmeldingId)?.let {
                egenmeldingFraSykmeldingRepository.delete(it)
            }
        }

        val egenmeldingsvar = sykmeldingKafkaMessage
            .event
            .sporsmals
            ?.firstOrNull { it.shortName == ShortNameDTO.EGENMELDINGSDAGER }
            ?.svar

        egenmeldingsvar?.let {
            if (egenmeldingFraSykmeldingRepository.findBySykmeldingId(sykmeldingId) == null) {
                egenmeldingFraSykmeldingRepository.save(
                    EgenmeldingFraSykmelding(
                        id = null,
                        sykmeldingId = sykmeldingId,
                        egenmeldingssvar = it
                    )
                )
            }
        }

        acknowledgment.acknowledge()
    }
}

fun String.tilSykmeldingKafkaMessage(): SykmeldingKafkaMessage = objectMapper.readValue(this)
