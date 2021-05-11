package no.nav.syfo

import com.nhaarman.mockitokotlin2.*
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.consumer.rest.juridisklogg.JuridiskLoggConsumer
import no.nav.syfo.consumer.rest.pdf.PDFRestConsumer
import no.nav.syfo.consumer.ws.client.AltinnConsumer
import no.nav.syfo.consumer.ws.client.OrganisasjonConsumer
import no.nav.syfo.domain.AltinnInnsendelseEkstraData
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.kafka.SYKEPENGESOKNAD_TOPIC
import no.nav.syfo.repository.SendtSoknadDao
import org.amshove.kluent.*
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.util.*

@DirtiesContext
class RebehandlingIntegrationTest : AbstractContainerBaseTest() {

    @Autowired
    private lateinit var aivenKafkaProducer: KafkaProducer<String, String>

    @Autowired
    private lateinit var sendtSoknadDao: SendtSoknadDao

    @MockBean
    private lateinit var pdlClient: PdlClient

    @MockBean
    private lateinit var altinnConsumer: AltinnConsumer

    @MockBean
    private lateinit var pdfRestConsumer: PDFRestConsumer

    @MockBean
    private lateinit var organisasjonConsumer: OrganisasjonConsumer

    @MockBean
    private lateinit var juridiskLoggConsumer: JuridiskLoggConsumer

    @Test
    fun `Sendt arbeidstaker søknad mottas, pdf generering feiler første gang, men neste gang går det og den sendes til altinn`() {
        val id = UUID.randomUUID().toString()
        val enkelSoknad = mockSykepengesoknadDTO.copy(id = id)

        whenever(pdlClient.hentFormattertNavn(enkelSoknad.fnr)).thenReturn("Ole Gunnar")
        whenever(pdfRestConsumer.getPDF(any(), any(), any())).thenThrow(RuntimeException("OOOPS")).thenReturn("pdf".toByteArray())

        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                null,
                id,
                enkelSoknad.serialisertTilString()
            )
        )

        // Det skal ta ca 10 sekunder grunnet rebehandlinga
        await().between(Duration.ofSeconds(8), Duration.ofSeconds(12))
            .until {
                sendtSoknadDao.soknadErSendt(id, false)
            }

        val sykepengesoknadCaptor: KArgumentCaptor<Sykepengesoknad> = argumentCaptor()
        val ekstradataCaptor: KArgumentCaptor<AltinnInnsendelseEkstraData> = argumentCaptor()

        verify(altinnConsumer).sendSykepengesoknadTilArbeidsgiver(sykepengesoknadCaptor.capture(), ekstradataCaptor.capture())
        verify(pdfRestConsumer, times(2)).getPDF(any(), any(), any())

        sykepengesoknadCaptor.lastValue.id `should be equal to` id
        sykepengesoknadCaptor.lastValue.fnr `should be equal to` "13068700000"

        ekstradataCaptor.lastValue.navn `should be equal to` "Ole Gunnar"
        ekstradataCaptor.lastValue.pdf `should be equal to` "pdf".toByteArray()
    }
}
