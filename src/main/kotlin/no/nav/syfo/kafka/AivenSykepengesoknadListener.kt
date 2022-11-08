package no.nav.syfo.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.sykepengesoknad.kafka.ArbeidssituasjonDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsstatusDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadstypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import no.nav.syfo.SendTilAltinnService
import no.nav.syfo.logger
import no.nav.syfo.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

const val SYKEPENGESOKNAD_TOPIC = "flex." + "sykepengesoknad"

@Component
class AivenSykepengesoknadListener(
    private val sendTilAltinnService: SendTilAltinnService,
    private val rebehandleSykepengesoknadProducer: RebehandleSykepengesoknadProducer,

) {

    private val log = logger()

    @KafkaListener(
        topics = [SYKEPENGESOKNAD_TOPIC],
        concurrency = "3",
        containerFactory = "aivenKafkaListenerContainerFactory",
        id = "sykepengesoknad-sendt",
        idIsGroup = false,
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {

        val sykepengesoknadDTO = cr.value().tilSykepengesoknadDTO()

        if (sykepengesoknadDTO.skalBehandles()) {
            val sykepengesoknad = sykepengesoknadDTO.konverter()
            try {
                sendTilAltinnService.sendSykepengesoknadTilAltinn(sykepengesoknad)
            } catch (e: Exception) {
                log.error("Feiler ved sending av søknad ${sykepengesoknadDTO.id}, legger til rebehandling", e)
                rebehandleSykepengesoknadProducer.send(sykepengesoknad)
            }
        } else {
            log.info("Ignorerer søknad ${sykepengesoknadDTO.id} med status ${sykepengesoknadDTO.status} og type ${sykepengesoknadDTO.type}")
        }

        acknowledgment.acknowledge()
    }

    fun String.tilSykepengesoknadDTO(): SykepengesoknadDTO = objectMapper.readValue(this)

    fun SykepengesoknadDTO.skalBehandles(): Boolean {
        return (
            this.type == SoknadstypeDTO.ARBEIDSTAKERE ||
                (this.type == SoknadstypeDTO.GRADERT_REISETILSKUDD && this.arbeidssituasjon == ArbeidssituasjonDTO.ARBEIDSTAKER) ||
                (this.type == SoknadstypeDTO.BEHANDLINGSDAGER && this.arbeidssituasjon == ArbeidssituasjonDTO.ARBEIDSTAKER)
            ) &&
            this.status == SoknadsstatusDTO.SENDT &&
            this.sendtArbeidsgiver != null
    }
}
