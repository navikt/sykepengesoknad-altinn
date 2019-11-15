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
    val soknadSomSkalReinnsendes = hentSoknaderSomSkalReinnsendes()
    var skalReinnsendeSoknader = true

    fun sendSykepengesoknadTilAltinn(sykepengesoknad: Sykepengesoknad) {
        val erEttersending = sykepengesoknad.ettersending
        val erIkkeReinnsending = erIkkeReinnsending(sykepengesoknad.id)
        if (erIkkeReinnsending && sendtSoknadDao.soknadErSendt(sykepengesoknad.id, erEttersending)) {
            log.warn("Forsøkte å sende søknad om sykepenger med id {} til Altinn som allerede er sendt", sykepengesoknad.id)
            return
        }
        sykepengesoknad.fnr = aktorRestConsumer.getFnr(sykepengesoknad.aktorId)
        sykepengesoknad.navn = personConsumer.finnBrukerPersonnavnByFnr(sykepengesoknad.fnr)
        sykepengesoknad.juridiskOrgnummerArbeidsgiver = organisasjonConsumer.hentJuridiskOrgnummer(sykepengesoknad.arbeidsgiver.orgnummer)
        sykepengesoknad.pdf = pdfRestController.getPDFArbeidstakere(sykepengesoknad)

        val validationeventer: MutableList<ValidationEvent> = mutableListOf()
        sykepengesoknad.xml = sykepengesoknad2XMLByteArray(sykepengesoknad, validationeventer)

        var receiptId: Int? = null
        if (validationeventer.isEmpty()) {

            if (erIkkeReinnsending) {
                receiptId = altinnConsumer.sendSykepengesoknadTilArbeidsgiver(sykepengesoknad)
                if (erEttersending) {
                    sendtSoknadDao.lagreEttersendtSoknad(sykepengesoknad.id, receiptId.toString())
                } else {
                    sendtSoknadDao.lagreSendtSoknad(SendtSoknad(sykepengesoknad.id, receiptId.toString(), now()))
                }
                registry.counter("syfoaltinn.soknadSendtTilAltinn", Tags.of("type", "info")).increment()
            } else {
                if (skalReinnsendeSoknader) {
                    receiptId = altinnConsumer.sendSykepengesoknadTilArbeidsgiver(sykepengesoknad)
                    log.info("Reinnsending av søknad ${sykepengesoknad.id}, legger ikke denne i basen")
                } else {
                    log.info("Ignorerer søknad ${sykepengesoknad.id} for å unngå duplikater i Altinn")
                }
            }

        } else {
            val feil = validationeventer.joinToString("\n") { it.message }
            log.error("Validering feilet for sykepengesøknad med id ${sykepengesoknad.id} med følgende feil: $feil")
            throw RuntimeException("Validering feilet for sykepengesøknad med id ${sykepengesoknad.id} med følgende feil: $feil")
        }

        try {
            if (receiptId != null) {
                juridiskLoggConsumer.lagreIJuridiskLogg(sykepengesoknad, receiptId)
            }
        } catch (e: JuridiskLoggException) {
            log.warn("Ved innsending av sykepengesøknad: ${sykepengesoknad.id} feilet juridisk logging")
        }
    }

    private fun hentSoknaderSomSkalReinnsendes(): MutableSet<String> {
        val resource = Application::class.java.getResourceAsStream("/Reinnsending.txt")
        val alleSoknadIdSomSkalReinnsendes: MutableSet<String> = mutableSetOf()
        resource.bufferedReader().lines().forEach { alleSoknadIdSomSkalReinnsendes.add(it) }
        return alleSoknadIdSomSkalReinnsendes
    }

    private fun erIkkeReinnsending(soknadId: String): Boolean {
        if ("c1b4d31e-b03f-4292-9be6-627c6ee65dad" == soknadId) {
            skalReinnsendeSoknader = false
        } else if ("dfa485a9-bdd6-498f-9870-c05571b42afc" == soknadId) {
            skalReinnsendeSoknader = true
        }
        if(soknadSomSkalReinnsendes.contains(soknadId)) {
            return false
        }
        return true
    }
}
