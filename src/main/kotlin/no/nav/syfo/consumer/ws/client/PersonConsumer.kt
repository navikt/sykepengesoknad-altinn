package no.nav.syfo.consumer.ws.client

import net.logstash.logback.encoder.org.apache.commons.lang.WordUtils.capitalizeFully
import no.nav.syfo.log
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonnavnBolkRequest
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
class PersonConsumer @Inject
constructor(private val personV3: PersonV3) {
    val log = log()

    fun finnBrukerPersonnavnByFnr(fnr: String): String? {
        return personV3.hentPersonnavnBolk(HentPersonnavnBolkRequest()
                .withAktoerListe(PersonIdent().withIdent(NorskIdent().withIdent(fnr))))
                .aktoerHarNavnListe
                .stream()
                .map { it.personnavn }
                .map { this.fulltNavn(it) }
                .findFirst()
                .orElse(null)
    }

    private fun fulltNavn(personnavn: Personnavn): String {
        val navn = when {
            personnavn.fornavn.isNullOrEmpty() -> personnavn.etternavn
            personnavn.mellomnavn.isNullOrEmpty() -> personnavn.fornavn + " " + personnavn.etternavn
            else -> personnavn.fornavn + " " + personnavn.mellomnavn + " " + personnavn.etternavn
        }
        return capitalizeFully(navn, charArrayOf(' ', '-'))
    }
}