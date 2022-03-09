package no.nav.syfo

import no.nav.syfo.repository.SendtSoknadDao
import org.amshove.kluent.*
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.util.*

class IntegrationTest : Testoppsett() {

    @Autowired
    private lateinit var sendtSoknadDao: SendtSoknadDao

    @Test
    fun `Sendt arbeidstaker søknad mottas og sendes til altinn`() {
        val id = UUID.randomUUID().toString()
        val enkelSoknad = mockSykepengesoknadDTO.copy(id = id)

        mockPdlResponse()
        mockAltinnResponse()

        leggSøknadPåKafka(enkelSoknad)

        // Håndterer duplikat
        leggSøknadPåKafka(enkelSoknad)

        await().atMost(Duration.ofSeconds(10))
            .until {
                sendtSoknadDao.soknadErSendt(id)
            }

        pdlMockWebserver.takeRequest()
        val altinnRequest = altinnMockWebserver.takeRequest().parseCorrespondence()
        altinnRequest.externalShipmentReference `should be equal to` id
        val correspondence = altinnRequest.correspondence
        correspondence.serviceCode.value `should be equal to` "4751"
        correspondence.content.value.messageTitle.value `should be equal to` "Søknad om sykepenger - 01.01.2019-09.01.2019 - Ole Gunnar (13068700000) - sendt til NAV"

        val attachments = correspondence.content.value.attachments.value.binaryAttachments.value.binaryAttachmentV2
        attachments shouldHaveSize 2
        attachments[0].fileName.value `should be equal to` "Sykepengesøknad.pdf"
        attachments[1].fileName.value `should be equal to` "sykepengesoeknad.xml"
    }
}
