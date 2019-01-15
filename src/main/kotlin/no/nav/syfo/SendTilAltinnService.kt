package no.nav.syfo

import no.nav.melding.virksomhet.sykepengesoeknadarbeidsgiver.v1.sykepengesoeknadarbeidsgiver.ObjectFactory
import no.nav.syfo.consumer.ws.client.AktorConsumer
import no.nav.syfo.consumer.ws.client.AltinnConsumer
import no.nav.syfo.consumer.ws.client.OrganisasjonConsumer
import no.nav.syfo.consumer.ws.client.PersonConsumer
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.rest.PDFRestController
import no.nav.syfo.util.JAXB
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
        val validationEventer: MutableList<ValidationEvent> = mutableListOf()
        sykepengesoknad.xml = JAXB.marshallSykepengesoeknadArbeidsgiver(
                ObjectFactory().createSykepengesoeknadArbeidsgiver(sykepengesoeknadArbeidsgiver2XML(sykepengesoknad))
        ) { event ->
            validationEventer.add(event)
            true
        }.toByteArray()

        if (validationEventer.isEmpty()) {
            altinnConsumer.sendSykepengesoknadTilArbeidsgiver(sykepengesoknad)
        } else {
            val feil = validationEventer.joinToString("\n") { it.message }
            log.error("Validering feilet for sykepengesøknad med id ${sykepengesoknad.id} med følgende feil: $feil")
            throw RuntimeException("Validering feilet for sykepengesøknad med id ${sykepengesoknad.id} med følgende feil: $feil")
        }
    }

}