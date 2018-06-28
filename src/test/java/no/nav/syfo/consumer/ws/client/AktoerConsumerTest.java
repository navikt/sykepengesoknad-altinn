package no.nav.syfo.consumer.ws.client;

import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2;
import no.nav.tjeneste.virksomhet.aktoer.v2.HentIdentForAktoerIdPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentIdentForAktoerIdRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentIdentForAktoerIdResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AktoerConsumerTest {

    @Mock
    private AktoerV2 aktoerV2;

    @InjectMocks
    private AktoerConsumer aktoerConsumer;

    @Test
    public void finnFnr() throws HentIdentForAktoerIdPersonIkkeFunnet {
        when(aktoerV2.hentIdentForAktoerId(new WSHentIdentForAktoerIdRequest().withAktoerId("aktoerId")))
                .thenReturn(new WSHentIdentForAktoerIdResponse().withIdent("fnr"));

        String fnr = aktoerConsumer.finnFnr("aktoerId");
        assertThat(fnr).isEqualTo("fnr");
    }
}