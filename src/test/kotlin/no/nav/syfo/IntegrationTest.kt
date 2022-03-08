package no.nav.syfo

import com.nhaarman.mockitokotlin2.*
import no.nav.syfo.client.altinn.AltinnClient
import no.nav.syfo.client.pdl.PdlClient
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
class IntegrationTest : AbstractContainerBaseTest() {

    @Autowired
    private lateinit var aivenKafkaProducer: KafkaProducer<String, String>

    @Autowired
    private lateinit var sendtSoknadDao: SendtSoknadDao

    @MockBean
    private lateinit var pdlClient: PdlClient

    @MockBean
    private lateinit var altinnConsumer: AltinnClient

    @Test
    fun `Sendt arbeidstaker søknad mottas og sendes til altinn`() {
        val id = UUID.randomUUID().toString()
        val enkelSoknad = mockSykepengesoknadDTO.copy(id = id)

        whenever(pdlClient.hentFormattertNavn(enkelSoknad.fnr)).thenReturn("Ole Gunnar")

        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                null,
                id,
                enkelSoknad.serialisertTilString()
            )
        )

        // Håndterer duplikat
        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                null,
                id,
                enkelSoknad.serialisertTilString()
            )
        )

        await().atMost(Duration.ofSeconds(10))
            .until {
                sendtSoknadDao.soknadErSendt(id, false)
            }

        val sykepengesoknadCaptor: KArgumentCaptor<Sykepengesoknad> = argumentCaptor()
        val ekstradataCaptor: KArgumentCaptor<AltinnInnsendelseEkstraData> = argumentCaptor()

        verify(altinnConsumer).sendSykepengesoknadTilArbeidsgiver(sykepengesoknadCaptor.capture(), ekstradataCaptor.capture())

        sykepengesoknadCaptor.lastValue.id `should be equal to` id
        sykepengesoknadCaptor.lastValue.fnr `should be equal to` "13068700000"

        ekstradataCaptor.lastValue.navn `should be equal to` "Ole Gunnar"
        ekstradataCaptor.lastValue.pdf.shouldNotBeNull()
    }
}
