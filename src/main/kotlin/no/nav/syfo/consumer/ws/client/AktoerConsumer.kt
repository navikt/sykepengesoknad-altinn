package no.nav.syfo.consumer.ws.client

import no.nav.syfo.log
import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2
import no.nav.tjeneste.virksomhet.aktoer.v2.HentIdentForAktoerIdPersonIkkeFunnet
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentIdentForAktoerIdRequest
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils.isEmpty
import javax.inject.Inject

@Component
class AktoerConsumer @Inject
constructor(private val aktoerV2: AktoerV2) {
    val log = log()

    fun finnFnr(aktoerId: String): String {
        if (isEmpty(aktoerId)) {
            throw RuntimeException("Kan ikke slå opp i AktørId-tjenesten: Har ikke aktør-id.")
        }

        try {
            val fnr = aktoerV2.hentIdentForAktoerId(WSHentIdentForAktoerIdRequest().withAktoerId(aktoerId)).ident
            log.info("fant fnr for aktørid")

            return fnr
        } catch (e: HentIdentForAktoerIdPersonIkkeFunnet) {
            log.error("Fnr ikke funnet", e)
            throw RuntimeException("Fant ikke fnr for aktørId: $aktoerId")
        }

    }
}
