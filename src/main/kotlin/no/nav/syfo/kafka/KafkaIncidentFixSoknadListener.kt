package no.nav.syfo.kafka

import no.nav.syfo.SendTilAltinnService
import no.nav.syfo.kafka.felles.ArbeidssituasjonDTO.ARBEIDSTAKER
import no.nav.syfo.kafka.felles.SoknadsstatusDTO.SENDT
import no.nav.syfo.kafka.felles.SoknadstypeDTO.BEHANDLINGSDAGER
import no.nav.syfo.kafka.felles.SoknadstypeDTO.ARBEIDSTAKERE
import no.nav.syfo.kafka.felles.SykepengesoknadDTO

import no.nav.syfo.log
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

const val groupid = "syfoaltinn-kafkaincident-full-rekjoring-5"
const val factory = "incidentFixKafkaListenerContainerFactory"

@Component
class KafkaIncidentFixSoknadListener @Inject
constructor(private val sendTilAltinnService: SendTilAltinnService) {
    val log = log()

    var maxdagv2: LocalDate? = null
    var maxdagv3: LocalDate? = null

    @KafkaListener(
            topics = ["syfo-soknad-v2"],
            id = "kafkaIncidentSoknadSendtv2",
            idIsGroup = false,
            containerFactory = factory,
            groupId = groupid
    )
    fun listenV2(cr: List<ConsumerRecord<String, SykepengesoknadDTO>>, acknowledgment: Acknowledgment) {
        if (cr.isNotEmpty()) {
            try {

                behandleRecords(cr)

                val maxDag = Instant.ofEpochMilli(cr.map { it.timestamp() }.max()!!).atOffset(ZoneOffset.UTC).toLocalDate()
                if (maxdagv2 == null || maxDag.isAfter(maxdagv2)) {
                    maxdagv2 = maxDag
                    log.info("syfo-soknad-v2 prossesering er på $maxDag")
                }
            } catch (e: Exception) {
                log.error("Feil ved behandling av records", e)
            }
            acknowledgment.acknowledge()
        }
    }

    @KafkaListener(
            topics = ["syfo-soknad-v3"],
            id = "kafkaIncidentSoknadSendtv3",
            idIsGroup = false,
            containerFactory = factory,
            groupId = groupid
    )
    fun listenV3(cr: List<ConsumerRecord<String, SykepengesoknadDTO>>, acknowledgment: Acknowledgment) {

        if (cr.isNotEmpty()) {
            try {

                behandleRecords(cr)
                val maxDag = Instant.ofEpochMilli(cr.map { it.timestamp() }.max()!!).atOffset(ZoneOffset.UTC).toLocalDate()
                if (maxdagv2 == null || maxDag.isAfter(maxdagv3)) {
                    maxdagv3 = maxDag
                    log.info("syfo-soknad-v3 prossesering er på $maxDag")
                }
            } catch (e: Exception) {
                log.error("Feil ved behandling av records", e)
            }
            acknowledgment.acknowledge()
        }
    }

    private fun behandleRecords(crs: List<ConsumerRecord<String, SykepengesoknadDTO>>) {
        crs.forEach { cr ->
            try {
                MDC.put(NAV_CALLID, getSafeNavCallIdHeaderAsString(cr.headers()))

                val sykepengesoknadDTO = cr.value()

                if ((sykepengesoknadDTO.type == ARBEIDSTAKERE
                                || (sykepengesoknadDTO.type == BEHANDLINGSDAGER && sykepengesoknadDTO.arbeidssituasjon == ARBEIDSTAKER))
                        && sykepengesoknadDTO.status == SENDT
                        && sykepengesoknadDTO.sendtArbeidsgiver != null
                        && soknaddsIder.contains(sykepengesoknadDTO.id)) {
                    val sykepengesoknad = konverter(sykepengesoknadDTO)
                    log.info("Behandler søknad ${sykepengesoknadDTO.id} med status ${sykepengesoknadDTO.status} og type ${sykepengesoknadDTO.type}")

                    try {
                        sendTilAltinnService.sendSykepengesoknadTilAltinn(sykepengesoknad)
                    } catch (e: Exception) {
                        log.error("Feiler ved behandling av søknad ${sykepengesoknadDTO.id}, legger til rebehandling", e)
                    }
                }

            } catch (e: Exception) {
                log.error("Uventet feil ved mottak av søknad på topic: ${cr.topic()}", e)
            } finally {
                MDC.remove(NAV_CALLID)
            }
        }
    }
}
