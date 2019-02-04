package no.nav.syfo

import no.nav.syfo.consumer.rest.aktor.AktorRestConsumer
import no.nav.syfo.consumer.rest.juridisklogg.JuridiskLoggConsumer
import no.nav.syfo.consumer.rest.juridisklogg.JuridiskLoggException
import no.nav.syfo.consumer.rest.pdf.PDFRestController
import no.nav.syfo.consumer.ws.client.AltinnConsumer
import no.nav.syfo.consumer.ws.client.OrganisasjonConsumer
import no.nav.syfo.consumer.ws.client.PersonConsumer
import no.nav.syfo.domain.soknad.Sykepengesoknad
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SendTilAltinnServiceTest {

    @Mock
    private lateinit var aktorRestConsumer: AktorRestConsumer
    @Mock
    private lateinit var altinnConsumer: AltinnConsumer
    @Mock
    private lateinit var personConsumer: PersonConsumer
    @Mock
    private lateinit var pdfRestController: PDFRestController
    @Mock
    private lateinit var organisasjonConsumer: OrganisasjonConsumer
    @Mock
    private lateinit var juridiskLoggConsumer: JuridiskLoggConsumer

    @InjectMocks
    private lateinit var sendTilAltinnService: SendTilAltinnService

    @Before
    fun setup() {
        given(aktorRestConsumer.getFnr(any())).willReturn("fnr")
        given(personConsumer.finnBrukerPersonnavnByFnr(any())).willReturn("Navn Navnesen")
        given(organisasjonConsumer.hentJuridiskOrgnummer(any())).willReturn("Juridisk Orgnummer")
        given(pdfRestController.getPDFArbeidstakere(any())).willReturn("".toByteArray())
        given(altinnConsumer.sendSykepengesoknadTilArbeidsgiver(any())).willReturn(123)
    }

    @Test
    fun senderTilAltinnOgLoggerJuridisk() {
        sendTilAltinnService.sendSykepengesoknadTilAltinn(mockSykepengesoknad)

        Mockito.verify(juridiskLoggConsumer).lagreIJuriskLogg(any(), ArgumentMatchers.anyInt())
    }

    @Test
    fun feilIJuridiskStopperIkkeInnsending() {
        given(juridiskLoggConsumer.lagreIJuriskLogg(any(), any())).willThrow(JuridiskLoggException())

        sendTilAltinnService.sendSykepengesoknadTilAltinn(mockSykepengesoknad)
    }
}
