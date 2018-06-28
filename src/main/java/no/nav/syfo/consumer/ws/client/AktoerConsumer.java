package no.nav.syfo.consumer.ws.client;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2;
import no.nav.tjeneste.virksomhet.aktoer.v2.HentIdentForAktoerIdPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentIdentForAktoerIdRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static org.springframework.util.StringUtils.isEmpty;

@Component
@Slf4j
public class AktoerConsumer {

    private AktoerV2 aktoerV2;

    @Inject
    public AktoerConsumer(AktoerV2 aktoerV2) {
        this.aktoerV2 = aktoerV2;
    }

    public String finnFnr(String aktoerId) {
        if (isEmpty(aktoerId)) {
            throw new RuntimeException("Kan ikke slå opp i AktørId-tjenesten: Har ikke aktør-id.");
        }

        try {
            String fnr = aktoerV2.hentIdentForAktoerId(new WSHentIdentForAktoerIdRequest().withAktoerId(aktoerId)).getIdent();
            log.info("fant fnr for aktørid");

            return fnr;
        } catch (HentIdentForAktoerIdPersonIkkeFunnet e) {
            log.error("Fnr ikke funnet", e);
            throw new RuntimeException("Fant ikke fnr for aktørId: " + aktoerId);
        }
    }
}
