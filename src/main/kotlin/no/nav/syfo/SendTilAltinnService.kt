package no.nav.syfo

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.syfo.consumer.rest.aktor.AktorRestConsumer
import no.nav.syfo.consumer.rest.juridisklogg.JuridiskLoggConsumer
import no.nav.syfo.consumer.rest.juridisklogg.JuridiskLoggException
import no.nav.syfo.consumer.rest.pdf.PDFRestController
import no.nav.syfo.consumer.ws.client.AltinnConsumer
import no.nav.syfo.consumer.ws.client.OrganisasjonConsumer
import no.nav.syfo.consumer.ws.client.PersonConsumer
import no.nav.syfo.domain.SendtSoknad
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.repository.SendtSoknadDao
import org.springframework.stereotype.Service
import java.time.LocalDateTime.now
import javax.xml.bind.ValidationEvent


@Service
class SendTilAltinnService(
        private val aktorRestConsumer: AktorRestConsumer,
        private val personConsumer: PersonConsumer,
        private val altinnConsumer: AltinnConsumer,
        private val pdfRestController: PDFRestController,
        private val organisasjonConsumer: OrganisasjonConsumer,
        private val juridiskLoggConsumer: JuridiskLoggConsumer,
        private val sendtSoknadDao: SendtSoknadDao,
        private val registry: MeterRegistry) {

    val log = log()

    fun sendSykepengesoknadTilAltinn(sykepengesoknad: Sykepengesoknad) {
        if (sendtSoknadDao.soknadErSendt(sykepengesoknad.id)) {
            log.warn("Forsøkte å sende søknad om sykepenger med id {} til Altinn som allerede er sendt", sykepengesoknad.id)
            return
        }
        sykepengesoknad.fnr = aktorRestConsumer.getFnr(sykepengesoknad.aktorId)
        sykepengesoknad.navn = personConsumer.finnBrukerPersonnavnByFnr(sykepengesoknad.fnr)
        sykepengesoknad.juridiskOrgnummerArbeidsgiver = organisasjonConsumer.hentJuridiskOrgnummer(sykepengesoknad.arbeidsgiver.orgnummer)
        sykepengesoknad.pdf = pdfRestController.getPDFArbeidstakere(sykepengesoknad)

        val validationeventer: MutableList<ValidationEvent> = mutableListOf()
        sykepengesoknad.xml = sykepengesoknad2XMLByteArray(sykepengesoknad, validationeventer)

        val receiptId: Int?
        if (validationeventer.isEmpty()) {
            receiptId = altinnConsumer.sendSykepengesoknadTilArbeidsgiver(sykepengesoknad)
            sendtSoknadDao.lagreSendtSoknad(SendtSoknad(sykepengesoknad.id, Integer.toString(receiptId), now()))
            registry.counter("syfoaltinn.soknadSendtTilAltinn", Tags.of("type", "info"))
        } else {
            val feil = validationeventer.joinToString("\n") { it.message }
            log.error("Validering feilet for sykepengesøknad med id ${sykepengesoknad.id} med følgende feil: $feil")
            throw RuntimeException("Validering feilet for sykepengesøknad med id ${sykepengesoknad.id} med følgende feil: $feil")
        }

        try {
            juridiskLoggConsumer.lagreIJuridiskLogg(sykepengesoknad, receiptId)
        } catch (e: JuridiskLoggException) {
            log.warn("Ved innsending av sykepengesøknad: ${sykepengesoknad.id} feilet juridisk logging")
        }
    }
}
