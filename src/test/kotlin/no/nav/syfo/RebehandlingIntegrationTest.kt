package no.nav.syfo

import no.nav.syfo.kafka.SYKEPENGESOKNAD_TOPIC
import no.nav.syfo.repository.SendtSoknadDao
import okhttp3.mockwebserver.MockResponse
import org.amshove.kluent.*
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.util.*

@DirtiesContext
class RebehandlingIntegrationTest : Testoppsett() {

    @Autowired
    private lateinit var aivenKafkaProducer: KafkaProducer<String, String>

    @Autowired
    private lateinit var sendtSoknadDao: SendtSoknadDao

    @Test
    fun `Sendt arbeidstaker søknad mottas, altinn kall feiler første gang, men neste gang går det og den sendes til altinn`() {
        val id = UUID.randomUUID().toString()
        val enkelSoknad = mockSykepengesoknadDTO.copy(id = id)

        val errorResponse = MockResponse()
            .setBody("ERRÅRRR")
            .setResponseCode(500)

        // 3 ganger grunnet retryable
        pdlMockWebserver.enqueue(errorResponse)
        pdlMockWebserver.enqueue(errorResponse)
        pdlMockWebserver.enqueue(errorResponse)

        mockPdlResponse()
        mockAltinnResponse()

        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                null,
                id,
                enkelSoknad.serialisertTilString()
            )
        )

        // Det skal ta ca 10 sekunder grunnet rebehandlinga
        await().between(Duration.ofSeconds(8), Duration.ofSeconds(30))
            .until {
                sendtSoknadDao.soknadErSendt(id, false)
            }

        val altinnRequest = altinnMockWebserver.takeRequest().parseCorrespondence()
        altinnRequest.externalShipmentReference `should be equal to` id

        repeat(4) {
            pdlMockWebserver.takeRequest()
        }
    }
}
