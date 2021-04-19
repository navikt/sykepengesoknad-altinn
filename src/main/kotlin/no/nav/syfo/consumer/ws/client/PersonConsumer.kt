package no.nav.syfo.consumer.ws.client

import no.nav.syfo.logger
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonnavnBolkRequest
import org.apache.commons.text.WordUtils.capitalizeFully
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
class PersonConsumer @Inject
constructor(private val personV3: PersonV3) {
    val log = logger()

    @Retryable(backoff = Backoff(delay = 5000))
    fun finnBrukerPersonnavnByFnr(fnr: String): String {
        return (
            personV3.hentPersonnavnBolk(
                HentPersonnavnBolkRequest()
                    .withAktoerListe(PersonIdent().withIdent(NorskIdent().withIdent(fnr)))
            )
                .aktoerHarNavnListe
                .map { it.personnavn }
                .map { fulltNavn(it) }
                .firstOrNull()
                ?: run {
                    log.error("finner ikke brukers personnavn")
                    throw RuntimeException("finner ikke brukers personnavn")
                }
            )
    }

    private fun fulltNavn(personnavn: Personnavn): String {
        val navn = when {
            personnavn.fornavn.isNullOrEmpty() -> personnavn.etternavn
            personnavn.mellomnavn.isNullOrEmpty() -> personnavn.fornavn + " " + personnavn.etternavn
            else -> personnavn.fornavn + " " + personnavn.mellomnavn + " " + personnavn.etternavn
        }
        return capitalizeFully(navn, ' ', '-')
    }
}
