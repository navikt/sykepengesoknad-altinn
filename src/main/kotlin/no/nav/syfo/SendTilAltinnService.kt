package no.nav.syfo

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.consumer.rest.pdf.PDFRestConsumer
import no.nav.syfo.consumer.ws.client.AltinnConsumer
import no.nav.syfo.consumer.ws.client.OrganisasjonConsumer
import no.nav.syfo.domain.AltinnInnsendelseEkstraData
import no.nav.syfo.domain.SendtSoknad
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.repository.SendtSoknadDao
import org.springframework.stereotype.Service
import java.time.LocalDateTime.now
import javax.xml.bind.ValidationEvent

@Service
class SendTilAltinnService(
    private val altinnConsumer: AltinnConsumer,
    private val pdfRestConsumer: PDFRestConsumer,
    private val organisasjonConsumer: OrganisasjonConsumer,
    private val sendtSoknadDao: SendtSoknadDao,
    private val registry: MeterRegistry,
    private val pdlClient: PdlClient,
) {

    val log = logger()

    @Synchronized
    fun sendSykepengesoknadTilAltinn(sykepengesoknad: Sykepengesoknad) {
        val erEttersending = sykepengesoknad.ettersending
        if (sendtSoknadDao.soknadErSendt(sykepengesoknad.id, erEttersending)) {
            log.warn("Forsøkte å sende søknad om sykepenger med id ${sykepengesoknad.id} til Altinn som allerede er sendt")
            return
        } else if (ettersendtTilNAV(sykepengesoknad)) {
            log.info("Behandler ikke ettersending til NAV for ${sykepengesoknad.id}")
            return
        }
        val fnr = sykepengesoknad.fnr
        val navn = pdlClient.hentFormattertNavn(fnr)

        val pdf = pdfRestConsumer.getPDF(sykepengesoknad, fnr, navn)
        val validationeventer: MutableList<ValidationEvent> = mutableListOf()
        val juridiskOrgnummerArbeidsgiver =
            organisasjonConsumer.hentJuridiskOrgnummer(sykepengesoknad.arbeidsgiver.orgnummer)
        val xml = sykepengesoknad2XMLByteArray(sykepengesoknad, validationeventer, fnr, juridiskOrgnummerArbeidsgiver)

        val ekstraData = AltinnInnsendelseEkstraData(
            fnr = fnr,
            navn = navn,
            pdf = pdf,
            xml = xml
        )

        val receiptId: Int?
        if (validationeventer.isEmpty()) {
            receiptId = altinnConsumer.sendSykepengesoknadTilArbeidsgiver(sykepengesoknad, ekstraData)
            if (erEttersending) {
                sendtSoknadDao.lagreEttersendtSoknad(sykepengesoknad.id, receiptId.toString())
            } else {
                sendtSoknadDao.lagreSendtSoknad(SendtSoknad(sykepengesoknad.id, receiptId.toString(), now()))
            }
            registry.counter("sykepengesoknad-altinn.soknadSendtTilAltinn", Tags.of("type", "info")).increment()
        } else {
            val feil = validationeventer.joinToString("\n") { it.message }
            log.error("Validering feilet for sykepengesøknad med id ${sykepengesoknad.id} med følgende feil: $feil")
            throw RuntimeException("Validering feilet for sykepengesøknad med id ${sykepengesoknad.id} med følgende feil: $feil")
        }
    }

    private fun ettersendtTilNAV(sykepengesoknad: Sykepengesoknad) = sykepengesoknad.sendtNav != null &&
        sykepengesoknad.sendtArbeidsgiver?.isBefore(sykepengesoknad.sendtNav) ?: false
}
