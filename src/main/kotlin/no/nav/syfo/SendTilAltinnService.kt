package no.nav.syfo

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.helse.flex.sykepengesoknad.arbeidsgiverwhitelist.whitelistetForArbeidsgiver
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import no.nav.syfo.client.altinn.AltinnClient
import no.nav.syfo.client.pdf.PDFClient
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.domain.AltinnInnsendelseEkstraData
import no.nav.syfo.domain.SendtSoknad
import no.nav.syfo.egenmelding.EgenmeldingFraSykmeldingRepository
import no.nav.syfo.egenmelding.egenmeldingsdager
import no.nav.syfo.kafka.konverter
import no.nav.syfo.orgnummer.JuridiskOrgnummerRepository
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
    private val juridiskOrgnummerRepository: JuridiskOrgnummerRepository,
    private val egenmeldingFraSykmeldingRepository: EgenmeldingFraSykmeldingRepository,
) {
    val log = logger()

    fun sendSykepengesoknadTilAltinn(sykepengesoknadDTO: SykepengesoknadDTO) {
        if (sendtSoknadRepository.existsBySykepengesoknadId(sykepengesoknadDTO.id)) {
            log.info("Forsøkte å sende søknad om sykepenger med id ${sykepengesoknadDTO.id} til Altinn som allerede er sendt")
            return
        }
        if (ettersendtTilNAV(sykepengesoknadDTO)) {
            log.info("Behandler ikke ettersending til NAV for ${sykepengesoknadDTO.id}")
            return
        }
        val sykepengesoknad = sykepengesoknadDTO.whitelistetForArbeidsgiver().konverter()
        val fnr = sykepengesoknad.fnr
        val navn = pdlClient.hentFormattertNavn(fnr)

        val pdf = pdfClient.getPDF(sykepengesoknad, fnr, navn)
        val validationeventer: MutableList<ValidationEvent> = mutableListOf()
        val orgnrFraDb =
            juridiskOrgnummerRepository.findBySykmeldingId(
                sykmeldingId = sykepengesoknad.sykmeldingId!!,
            ) ?: throw RuntimeException("Mangler orgnummer i databasen")
        val juridiskOrgnummerArbeidsgiver = orgnrFraDb.juridiskOrgnummer ?: sykepengesoknad.arbeidsgiver.orgnummer

        val egenmeldingsvar =
            egenmeldingFraSykmeldingRepository
                .findBySykmeldingId(
                    sykmeldingId = sykepengesoknad.sykmeldingId,
                )?.egenmeldingsdager()
        val xml = sykepengesoknad2XMLByteArray(sykepengesoknad, validationeventer, fnr, juridiskOrgnummerArbeidsgiver, egenmeldingsvar)

        val ekstraData =
            AltinnInnsendelseEkstraData(
                fnr = fnr,
                navn = navn,
                pdf = pdf,
                xml = xml,
            )

        if (validationeventer.isEmpty()) {
            altinnClient.sendSykepengesoknadTilArbeidsgiver(sykepengesoknad, ekstraData)
            sendtSoknadRepository.save(SendtSoknad(null, sykepengesoknad.id, Instant.now()))
            registry.counter("sykepengesoknad-altinn.soknadSendtTilAltinn", Tags.of("type", "info")).increment()
            log.info("Sykepengesøknad ${sykepengesoknad.id} ble sendt til altinn")
        } else {
            val feil = validationeventer.joinToString("\n") { it.message }
            log.error("Validering feilet for sykepengesøknad med id ${sykepengesoknad.id} med følgende feil: $feil")
            throw RuntimeException("Validering feilet for sykepengesøknad med id ${sykepengesoknad.id} med følgende feil: $feil")
        }
    }

    private fun ettersendtTilNAV(sykepengesoknadDTO: SykepengesoknadDTO) =
        sykepengesoknadDTO.sendtNav != null &&
            sykepengesoknadDTO.sendtArbeidsgiver?.isBefore(sykepengesoknadDTO.sendtNav) ?: false
}
