package no.nav.syfo.kafka

import no.nav.syfo.CALL_ID
import no.nav.syfo.SendTilAltinnService
import no.nav.syfo.domain.soknad.Soknadsstatus
import no.nav.syfo.domain.soknad.Soknadstype
import no.nav.syfo.kafka.KafkaHeaderConstants.getLastHeaderByKeyAsString
import no.nav.syfo.kafka.interfaces.Soknad
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import no.nav.syfo.log
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.util.UUID.randomUUID
import javax.inject.Inject

@Component
class SoknadListener @Inject
constructor(private val sendTilAltinnService: SendTilAltinnService) {

    val log = log()
    var sendtForsteSoknad = false

    @KafkaListener(topics = ["syfo-soknad-v2"], id = "soknadSendt", idIsGroup = false)
    fun listen(cr: ConsumerRecord<String, Soknad>, acknowledgment: Acknowledgment) {
        log.info("Melding mottatt på topic: {} med offsett: {}", cr.topic(), cr.offset())

        try {
            MDC.put(CALL_ID, getLastHeaderByKeyAsString(cr.headers(), CALL_ID).orElse(randomUUID().toString()))

            val sykepengesoknad = konverter(cr.value() as SykepengesoknadDTO)

            if (Soknadstype.ARBEIDSTAKERE == sykepengesoknad.type
                    && Soknadsstatus.SENDT == sykepengesoknad.status) {

                log.info("har plukket opp søknad: {}", sykepengesoknad.toString())

                //TODO behandle alle innsendte søknader (nå behandles bare den første)
                if (!sendtForsteSoknad) {
                    sendtForsteSoknad = true
                    val sendSykepengesoknadTilArbeidsgiver = sendTilAltinnService.sendSykepengesoknadTilAltinn(sykepengesoknad)
                    //TODO denne må også logges til juridisk logg
                    log.info("Får denne kvitteringen etter innsending til altinn: $sendSykepengesoknadTilArbeidsgiver")
                }
                log.info("ignorerer foreløpig den mottatte søknaden")

            }

            acknowledgment.acknowledge()
        } catch (e: Exception) {
            log.error("Uventet feil ved behandling av søknad", e)

            //TODO putt på feil-topic

            throw RuntimeException("Uventet feil ved behandling av søknad")
        } finally {
            MDC.remove(CALL_ID)
        }
    }
}
