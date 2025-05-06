package no.nav.syfo

import no.nav.syfo.orgnummer.JuridiskOrgnummer
import okhttp3.mockwebserver.MockResponse
import org.amshove.kluent.*
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.*

class RebehandlingIntegrationTest : FellesTestOppsett() {
    @Test
    fun `Sendt arbeidstaker søknad mottas, altinn kall feiler første gang, men neste gang går det og den sendes til altinn`() {
        val id = UUID.randomUUID().toString()
        val enkelSoknad = mockSykepengesoknadDTO.copy(id = id)
        juridiskOrgnummerRepository.save(
            JuridiskOrgnummer(orgnummer = "12345678", juridiskOrgnummer = "LEGAL123", sykmeldingId = enkelSoknad.sykmeldingId!!),
        )

        val errorResponse =
            MockResponse()
                .setBody("ERRÅRRR")
                .setResponseCode(500)

        // 3 ganger grunnet retryable
        pdlMockWebserver.enqueue(errorResponse)
        pdlMockWebserver.enqueue(errorResponse)
        pdlMockWebserver.enqueue(errorResponse)

        mockPdlResponse()
        mockAltinnResponse()

        leggSøknadPåKafka(enkelSoknad)

        // Det skal ta ca 10 sekunder grunnet rebehandlinga
        await()
            .between(Duration.ofSeconds(8), Duration.ofSeconds(30))
            .until {
                sendtSoknadRepository.existsBySykepengesoknadId(id)
            }

        val altinnRequest = altinnMockWebserver.takeRequest().parseCorrespondence()
        altinnRequest.externalShipmentReference `should be equal to` id

        repeat(4) {
            pdlMockWebserver.takeRequest()
        }
    }
}
