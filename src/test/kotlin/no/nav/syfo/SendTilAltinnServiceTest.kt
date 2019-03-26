package no.nav.syfo

import io.micrometer.core.instrument.MeterRegistry
import no.nav.syfo.consumer.rest.aktor.AktorRestConsumer
import no.nav.syfo.consumer.rest.juridisklogg.JuridiskLoggConsumer
import no.nav.syfo.consumer.rest.juridisklogg.JuridiskLoggException
import no.nav.syfo.consumer.rest.pdf.PDFRestController
import no.nav.syfo.consumer.ws.client.AltinnConsumer
import no.nav.syfo.consumer.ws.client.OrganisasjonConsumer
import no.nav.syfo.consumer.ws.client.PersonConsumer
import no.nav.syfo.repository.SendtSoknadDao
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
    @Mock
    private lateinit var sendtSoknadDao: SendtSoknadDao
    @Mock
    private lateinit var registry: MeterRegistry

    @InjectMocks
    private lateinit var sendTilAltinnService: SendTilAltinnService

    private val ressursId = "d053fef8-6f2e-4d45-bc9f-ed6c5cd457dd"

    @Before
    fun setup() {
        given(aktorRestConsumer.getFnr(any())).willReturn("fnr")
        given(personConsumer.finnBrukerPersonnavnByFnr(any())).willReturn("Navn Navnesen")
        given(organisasjonConsumer.hentJuridiskOrgnummer(any())).willReturn("Juridisk Orgnummer")
        given(pdfRestController.getPDFArbeidstakere(any())).willReturn("".toByteArray())
        given(altinnConsumer.sendSykepengesoknadTilArbeidsgiver(any())).willReturn(123)
        given(sendtSoknadDao.soknadErSendt(ressursId, false)).willReturn(false)
    }

    @Test
    fun senderTilAltinnOgLoggerJuridisk() {
        sendTilAltinnService.sendSykepengesoknadTilAltinn(mockSykepengesoknad)

        verify(juridiskLoggConsumer).lagreIJuridiskLogg(any(), anyInt())
        verify(sendtSoknadDao).soknadErSendt(ressursId, false)
        verify(sendtSoknadDao).lagreSendtSoknad(any())
    }

    @Test
    fun feilIJuridiskStopperIkkeInnsending() {
        given(juridiskLoggConsumer.lagreIJuridiskLogg(any(), any())).willThrow(JuridiskLoggException())

        sendTilAltinnService.sendSykepengesoknadTilAltinn(mockSykepengesoknad)

        verify(sendtSoknadDao).soknadErSendt(ressursId, false)
        verify(sendtSoknadDao).lagreSendtSoknad(any())
    }

    @Test
    fun senderIkkeTilAltinnHvisSoknadAlleredeErSendt() {
        given(sendtSoknadDao.soknadErSendt(ressursId, false)).willReturn(true)
        sendTilAltinnService.sendSykepengesoknadTilAltinn(mockSykepengesoknad)

        verify(sendtSoknadDao).soknadErSendt(ressursId, false)
        verify(altinnConsumer, Mockito.never()).sendSykepengesoknadTilArbeidsgiver(any())
        verify(sendtSoknadDao, Mockito.never()).lagreSendtSoknad(any())
    }
}
