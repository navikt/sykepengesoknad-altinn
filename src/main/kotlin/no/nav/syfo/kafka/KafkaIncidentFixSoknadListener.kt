package no.nav.syfo.kafka


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


@Component
class KafkaIncidentFixSoknadListener @Inject
constructor() {
    val log = log()

    var maxdagv2: LocalDate? = null

    @KafkaListener(
            topics = ["syfo-soknad-v2"],
            id = "kafkaIncidentSoknadSendtv2",
            idIsGroup = false,
            containerFactory = "incidentFixKafkaListenerContainerFactory",
            groupId = "syfoaltinn-kafkaincident-full-rekjoring-enkeltsok-1"
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


    private fun behandleRecords(crs: List<ConsumerRecord<String, SykepengesoknadDTO>>) {
        crs.forEach { cr ->
            try {
                MDC.put(NAV_CALLID, getSafeNavCallIdHeaderAsString(cr.headers()))

                val sykepengesoknadDTO = cr.value()
                if (sykepengesoknadDTO.id == "6f1a0649-5273-452a-8cd8-fc30e5ac8544") {
                    val time = Instant.ofEpochMilli(cr.timestamp()).atOffset(ZoneOffset.UTC).toZonedDateTime()
                    log.info("Fant søknad med if ${sykepengesoknadDTO.id} og status ${sykepengesoknadDTO.status} tdspunkt $time")
                }

            } catch (e: Exception) {
                log.error("Uventet feil ved mottak av søknad på topic: ${cr.topic()}", e)
            } finally {
                MDC.remove(NAV_CALLID)
            }
        }
    }
}
