package no.nav.syfo

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.syfo.client.altinn.AltinnClient
import no.nav.syfo.client.pdf.PDFClient
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.domain.AltinnInnsendelseEkstraData
import no.nav.syfo.domain.SendtSoknad
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.repository.SendtSoknadRepository
import org.springframework.stereotype.Service
import java.time.Instant
import javax.xml.bind.ValidationEvent

@Service
class SendTilAltinnService(
    private val altinnClient: AltinnClient,
    private val pdfClient: PDFClient,
    private val sendtSoknadRepository: SendtSoknadRepository,
    private val registry: MeterRegistry,
    private val pdlClient: PdlClient,
) {

    val log = logger()

    fun sendSykepengesoknadTilAltinn(sykepengesoknad: Sykepengesoknad) {
        if (sendtSoknadRepository.existsBySykepengesoknadId(sykepengesoknad.id)) {
            log.warn("Forsøkte å sende søknad om sykepenger med id ${sykepengesoknad.id} til Altinn som allerede er sendt")
            return
        }
        if (ettersendtTilNAV(sykepengesoknad)) {
            log.info("Behandler ikke ettersending til NAV for ${sykepengesoknad.id}")
            return
        }
        val fnr = sykepengesoknad.fnr
        val navn = pdlClient.hentFormattertNavn(fnr)

        val pdf = pdfClient.getPDF(sykepengesoknad, fnr, navn)
        val validationeventer: MutableList<ValidationEvent> = mutableListOf()
        val juridiskOrgnummerArbeidsgiver = sykepengesoknad.arbeidsgiver.orgnummer // TODO hent fra db

        val xml = sykepengesoknad2XMLByteArray(sykepengesoknad, validationeventer, fnr, juridiskOrgnummerArbeidsgiver)

        val ekstraData = AltinnInnsendelseEkstraData(
            fnr = fnr,
            navn = navn,
            pdf = pdf,
            xml = xml
        )

        if (validationeventer.isEmpty()) {
            altinnClient.sendSykepengesoknadTilArbeidsgiver(sykepengesoknad, ekstraData)
            sendtSoknadRepository.save(SendtSoknad(null, sykepengesoknad.id, Instant.now()))
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
