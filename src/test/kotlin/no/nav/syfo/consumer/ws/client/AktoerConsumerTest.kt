package no.nav.syfo.consumer.ws.client

import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2
import no.nav.tjeneste.virksomhet.aktoer.v2.HentIdentForAktoerIdPersonIkkeFunnet
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentIdentForAktoerIdRequest
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentIdentForAktoerIdResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AktoerConsumerTest {

    @Mock
    private val aktoerV2: AktoerV2? = null

    @InjectMocks
    private val aktoerConsumer: AktoerConsumer? = null

    @Test
    @Throws(HentIdentForAktoerIdPersonIkkeFunnet::class)
    fun finnFnr() {
        `when`(aktoerV2!!.hentIdentForAktoerId(WSHentIdentForAktoerIdRequest().withAktoerId("aktoerId")))
            .thenReturn(WSHentIdentForAktoerIdResponse().withIdent("fnr"))

        val fnr = aktoerConsumer!!.finnFnr("aktoerId")
        assertThat(fnr).isEqualTo("fnr")
    }
}