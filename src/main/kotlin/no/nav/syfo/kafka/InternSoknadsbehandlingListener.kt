package no.nav.syfo.kafka

import no.nav.syfo.CALL_ID
import no.nav.syfo.SendTilAltinnService
import no.nav.syfo.domain.soknad.Soknadsstatus
import no.nav.syfo.domain.soknad.Soknadstype
import no.nav.syfo.kafka.interfaces.Soknad
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import no.nav.syfo.log
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.util.*
import javax.inject.Inject

@Component
class InternSoknadsbehandlingListener @Inject
constructor(private val sendTilAltinnService: SendTilAltinnService,
            private val internSoknadsbehandlingProducer: InternSoknadsbehandlingProducer) {
    val log = log()

    @KafkaListener(topics = ["privat-syfoaltinn-soknad-v1"], id = "syfoaltinnIntern", idIsGroup = false)
    fun listen(cr: ConsumerRecord<String, Soknad>, acknowledgment: Acknowledgment) {
        log.info("Melding mottatt på topic: ${cr.topic()} med offsett: ${cr.offset()}")

        try {
            MDC.put(CALL_ID, KafkaHeaderConstants.getLastHeaderByKeyAsString(cr.headers(), CALL_ID).orElse(UUID.randomUUID().toString()))

            val sykepengesoknadDTO = cr.value() as SykepengesoknadDTO
            val sykepengesoknad = konverter(sykepengesoknadDTO)

            if (Soknadstype.ARBEIDSTAKERE == sykepengesoknad.type
                    && Soknadsstatus.SENDT == sykepengesoknad.status) {

                log.info("intern behandling av søknad: ${sykepengesoknad.id}")

                //TODO behandle alle innsendte søknader
                //val sendSykepengesoknadTilArbeidsgiver = sendTilAltinnService.sendSykepengesoknadTilAltinn(sykepengesoknad)
                //TODO denne må også logges til juridisk logg
                //log.info("Får denne kvitteringen etter innsending til altinn: $sendSykepengesoknadTilArbeidsgiver")

                log.info("ignorerer foreløpig søknaden")
            }
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            val sykepengesoknadDTO = cr.value() as SykepengesoknadDTO

            log.error("Uventet feil ved behandling av søknad ${sykepengesoknadDTO.id}", e)
            internSoknadsbehandlingProducer.leggPaInternTopic(sykepengesoknadDTO)
            log.info("Behandling feiler, legger søknad på intern topic")
            acknowledgment.acknowledge()
        } finally {
            MDC.remove(CALL_ID)
        }
    }
}