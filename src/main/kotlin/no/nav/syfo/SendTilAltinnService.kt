package no.nav.syfo

import no.nav.syfo.consumer.ws.client.AktorConsumer
import no.nav.syfo.consumer.ws.client.AltinnConsumer
import no.nav.syfo.consumer.ws.client.OrganisasjonConsumer
import no.nav.syfo.consumer.ws.client.PersonConsumer
import no.nav.syfo.domain.soknad.Sykepengesoknad
import org.springframework.stereotype.Service
import javax.inject.Inject


@Service
class SendTilAltinnService @Inject
constructor(private val aktorConsumer: AktorConsumer,
            private val personConsumer: PersonConsumer,
            private val altinnConsumer: AltinnConsumer,
            private val pdfRestController: PDFRestController,
            private val organisasjonConsumer: OrganisasjonConsumer) {

    val log = log()

    fun sendSykepengesoknadTilAltinn(sykepengesoknad: Sykepengesoknad) {

        val fnr = aktorConsumer.finnFnr(sykepengesoknad.aktorId)
        val navn = personConsumer.finnBrukerPersonnavnByFnr(fnr)
        val juridiskOrgnummerArbeidsgiver = organisasjonConsumer.hentJuridiskOrgnummer(sykepengesoknad.arbeidsgiver.orgnummer)
        val pdf = pdfRestController.getPDF(sykepengesoknad, PDFTemplate.ARBEIDSTAKERE)
        val xmlSykepengesoeknadArbeidsgiver = sykepengesoeknadArbeidsgiver2XML(sykepengesoknad, juridiskOrgnummerArbeidsgiver, fnr)

        val sykepengesoknadAltinn = SykepengesoknadAltinn(sykepengesoknad,
                fnr,
                navn,
                xmlSykepengesoeknadArbeidsgiver,
                pdf)


        //sykepengesoknad.getSykmeldingDokument().bruker.fnr = aktoerIdConsumer.finnFnr(sykepengesoknad.getSykmeldingDokument().bruker.aktoerId)
        //sykepengesoknad.getSykmeldingDokument().arbeidsgiver.juridiskOrgnummer = organisasjonService.hentJuridiskOrgnummer(sykepengesoknad.getSykmeldingDokument().arbeidsgiver.orgnummer)

        /*val metadata = Metadata(
                organisasjonService.hentNavn(sykepengesoknad.getSykmeldingDokument().arbeidsgiver.orgnummer),
                personConsumer.finnBrukerPersonnavnByFnr(sykepengesoknad.getSykmeldingDokument().getPasientFnr()),
                sykepengesoknad.sykepengesoeknadUuid,
                sykepengesoknad.korrigerer != null
        )*/


        //TODO validering
        if (sykepengesoknadAltinn.validationEventer.isEmpty()) {
            altinnConsumer.sendSykepengesoknadTilArbeidsgiver(sykepengesoknadAltinn)
        }
        /*val event = createEvent("antallDagerInnsendingSykepengesoeknad")
        val dager = DAYS.between(sykepengesoknad.tilgjengeliggjortDato(), now())
        event.addFieldToReport("dager", dager)
        event.report()*/
        /*} else {
            val feil = sykepengesoknadAltinn
                    .validationEventer
                    .stream()
                    .map(Function<ValidationEvent, Any> { ValidationEvent.getMessage() })
                    .collect(joining("\n"))
            log.error("Validering feilet for sykepengesøknad med id $sykepengesoeknadId med følgende feil: {}", feil)
            throw OppgaveValideringException("Validering feilet for sykepengesøknad med id $sykepengesoeknadId med følgende feil: $feil")
        }*/
    }

}