package no.nav.syfo

import no.nav.syfo.consumer.ws.client.AktorConsumer
import no.nav.syfo.consumer.ws.client.AltinnConsumer
import no.nav.syfo.consumer.ws.client.OrganisasjonConsumer
import no.nav.syfo.consumer.ws.client.PersonConsumer
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.rest.PDFRestController
import org.springframework.stereotype.Service
import javax.inject.Inject
import javax.xml.bind.ValidationEvent


@Service
class SendTilAltinnService @Inject
constructor(private val aktorConsumer: AktorConsumer,
            private val personConsumer: PersonConsumer,
            private val altinnConsumer: AltinnConsumer,
            private val pdfRestController: PDFRestController,
            private val organisasjonConsumer: OrganisasjonConsumer) {

    val log = log()

    fun sendSykepengesoknadTilAltinn(sykepengesoknad: Sykepengesoknad) {

        sykepengesoknad.fnr = aktorConsumer.finnFnr(sykepengesoknad.aktorId)
        sykepengesoknad.navn = personConsumer.finnBrukerPersonnavnByFnr(sykepengesoknad.fnr)
        sykepengesoknad.juridiskOrgnummerArbeidsgiver = organisasjonConsumer.hentJuridiskOrgnummer(sykepengesoknad.arbeidsgiver.orgnummer)
        sykepengesoknad.pdf = pdfRestController.getPDFArbeidstakere(sykepengesoknad)

        // TODO XMLSykepengesoeknadArbeidsgiver må logges til juridisk logg
        val validationeventer: MutableList<ValidationEvent> = mutableListOf()
        sykepengesoknad.xml = sykepengesoknad2XMLByteArray(sykepengesoknad, validationeventer)

        if (validationeventer.isEmpty()) {
            altinnConsumer.sendSykepengesoknadTilArbeidsgiver(sykepengesoknad)
        } else {
            val feil = validationeventer.joinToString("\n") { it.message }
            log.error("Validering feilet for sykepengesøknad med id ${sykepengesoknad.id} med følgende feil: $feil")
            throw RuntimeException("Validering feilet for sykepengesøknad med id ${sykepengesoknad.id} med følgende feil: $feil")
        }
    }

}