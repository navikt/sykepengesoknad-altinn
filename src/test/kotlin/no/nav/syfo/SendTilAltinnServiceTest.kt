package no.nav.syfo

import com.nhaarman.mockitokotlin2.*
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.syfo.consumer.rest.juridisklogg.JuridiskLoggConsumer
import no.nav.syfo.consumer.rest.juridisklogg.JuridiskLoggException
import no.nav.syfo.consumer.rest.pdf.PDFRestConsumer
import no.nav.syfo.consumer.ws.client.AltinnConsumer
import no.nav.syfo.consumer.ws.client.OrganisasjonConsumer
import no.nav.syfo.consumer.ws.client.PersonConsumer
import no.nav.syfo.repository.SendtSoknadDao
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SendTilAltinnServiceTest {

    @Mock
    private lateinit var altinnConsumer: AltinnConsumer
    @Mock
    private lateinit var personConsumer: PersonConsumer
    @Mock
    private lateinit var pdfRestConsumer: PDFRestConsumer
    @Mock
    private lateinit var organisasjonConsumer: OrganisasjonConsumer
    @Mock
    private lateinit var juridiskLoggConsumer: JuridiskLoggConsumer
    @Mock
    private lateinit var sendtSoknadDao: SendtSoknadDao
    @Mock
    private lateinit var registry: MeterRegistry
    @Mock
    private lateinit var counter: Counter

    @InjectMocks
    private lateinit var sendTilAltinnService: SendTilAltinnService

    private val ressursId = "d053fef8-6f2e-4d45-bc9f-ed6c5cd457dd"

    @BeforeEach
    fun setup() {
        given(personConsumer.finnBrukerPersonnavnByFnr(any())).willReturn("Navn Navnesen")
        given(organisasjonConsumer.hentJuridiskOrgnummer(any())).willReturn("Juridisk Orgnummer")
        given(pdfRestConsumer.getPDF(any(), any(), any())).willReturn("pdf".toByteArray())
        given(altinnConsumer.sendSykepengesoknadTilArbeidsgiver(any(), any())).willReturn(123)
        given(sendtSoknadDao.soknadErSendt(ressursId, false)).willReturn(false)
        given(registry.counter(ArgumentMatchers.anyString(), ArgumentMatchers.any(Tags::class.java))).willReturn(counter)
    }

    @Test
    fun senderTilAltinnOgLoggerJuridisk() {
        sendTilAltinnService.sendSykepengesoknadTilAltinn(mockSykepengesoknad.first)

        verify(juridiskLoggConsumer).lagreIJuridiskLogg(any(), any(), any())
        verify(sendtSoknadDao).soknadErSendt(ressursId, false)
        verify(sendtSoknadDao).lagreSendtSoknad(any())
    }

    @Test
    fun feilIJuridiskStopperIkkeInnsending() {
        given(juridiskLoggConsumer.lagreIJuridiskLogg(any(), any(), any())).willThrow(JuridiskLoggException())

        sendTilAltinnService.sendSykepengesoknadTilAltinn(mockSykepengesoknad.first)

        verify(sendtSoknadDao).soknadErSendt(ressursId, false)
        verify(sendtSoknadDao).lagreSendtSoknad(any())
    }

    @Test
    fun senderIkkeTilAltinnHvisSoknadAlleredeErSendt() {
        given(sendtSoknadDao.soknadErSendt(ressursId, false)).willReturn(true)
        sendTilAltinnService.sendSykepengesoknadTilAltinn(mockSykepengesoknad.first)

        verify(sendtSoknadDao).soknadErSendt(ressursId, false)
        verify(altinnConsumer, Mockito.never()).sendSykepengesoknadTilArbeidsgiver(any(), any())
        verify(sendtSoknadDao, Mockito.never()).lagreSendtSoknad(any())
    }

    @Test
    fun ettersendingTilNavBehandlesIkke() {
        val soknad = mockSykepengesoknad.first.copy(
            sendtArbeidsgiver = LocalDateTime.now().minusDays(1),
            sendtNav = LocalDateTime.now(),
            ettersending = true
        )
        sendTilAltinnService.sendSykepengesoknadTilAltinn(soknad)

        verify(altinnConsumer, Mockito.never()).sendSykepengesoknadTilArbeidsgiver(any(), any())
    }

    @Test
    fun ettersendingTilArbeidsgiver_OK() {
        val innsending1 = LocalDateTime.now().minusDays(1)
        val soknad1 = mockSykepengesoknad.first.copy(
            sendtArbeidsgiver = innsending1,
            sendtNav = innsending1,
            ettersending = false
        )
        sendTilAltinnService.sendSykepengesoknadTilAltinn(soknad1)
        verify(altinnConsumer, Mockito.times(1)).sendSykepengesoknadTilArbeidsgiver(any(), any())
        verify(sendtSoknadDao, Mockito.times(1)).lagreSendtSoknad(any())

        val innsending2 = LocalDateTime.now()
        val soknad2 = mockSykepengesoknad.first.copy(
            sendtArbeidsgiver = innsending2,
            sendtNav = innsending1,
            ettersending = true
        )
        sendTilAltinnService.sendSykepengesoknadTilAltinn(soknad2)
        verify(altinnConsumer, Mockito.times(2)).sendSykepengesoknadTilArbeidsgiver(any(), any())
        verify(sendtSoknadDao, Mockito.times(1)).lagreEttersendtSoknad(any(), any())
    }
}
