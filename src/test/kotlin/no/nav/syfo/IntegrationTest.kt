package no.nav.syfo

import com.google.cloud.storage.Storage
import no.nav.syfo.orgnummer.JuridiskOrgnummer
import org.amshove.kluent.*
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import java.time.Duration
import java.util.*

class IntegrationTest : FellesTestOppsett() {
    @Autowired
    private lateinit var storage: Storage

    @Value("\${BUCKET_NAME}")
    lateinit var bucketName: String

    @Test
    fun `Sendt arbeidstaker søknad mottas og sendes til altinn`() {
        val id = UUID.randomUUID().toString()
        val enkelSoknad = mockSykepengesoknadDTO.copy(id = id)
        juridiskOrgnummerRepository.save(
            JuridiskOrgnummer(orgnummer = "12345678", juridiskOrgnummer = "LEGAL123", sykmeldingId = enkelSoknad.sykmeldingId!!),
        )
        mockPdlResponse()
        mockAltinnResponse()

        leggSøknadPåKafka(enkelSoknad)

        // Håndterer duplikat
        leggSøknadPåKafka(enkelSoknad)

        await()
            .atMost(Duration.ofSeconds(10))
            .until {
                sendtSoknadRepository.existsBySykepengesoknadId(id)
            }

        pdlMockWebserver.takeRequest()
        val altinnRequest = altinnMockWebserver.takeRequest().parseCorrespondence()
        altinnRequest.externalShipmentReference `should be equal to` id
        val correspondence = altinnRequest.correspondence
        correspondence.serviceCode.value `should be equal to` "4751"
        correspondence.content.value.messageTitle.value `should be equal to`
            "Søknad om sykepenger - 01.01.2019-09.01.2019 - Ole Gunnar (13068700000) - sendt til NAV"

        val attachments = correspondence.content.value.attachments.value.binaryAttachments.value.binaryAttachmentV2
        attachments shouldHaveSize 2
        attachments[0].fileName.value `should be equal to` "Sykepengesøknad.pdf"
        attachments[1].fileName.value `should be equal to` "sykepengesoeknad.xml"

        val innhold = storage.list(bucketName).values.toList()
        val relaterteFiler = innhold.filter { it.name.contains(id) }
        relaterteFiler.shouldHaveSize(6)
        relaterteFiler.first { it.name.contains("sykepengesoknad.xml") }.contentType `should be equal to` "application/xml"
        relaterteFiler.first { it.name.contains("correspondence.xml") }.contentType `should be equal to` "application/xml"
        relaterteFiler.first { it.name.contains("correspondence.gz") }.contentType `should be equal to` "application/gzip"
        relaterteFiler.first { it.name.contains("receiptExternal.xml") }.contentType `should be equal to` "application/xml"
        relaterteFiler.first { it.name.contains("receiptExternal.gz") }.contentType `should be equal to` "application/gzip"
        relaterteFiler.first { it.name.contains("sykepengesoknad.pdf") }.contentType `should be equal to` "application/pdf"

        val soknadXml = relaterteFiler.first { it.name.contains("sykepengesoknad.xml") }.getContent().tilXMLSykepengesoeknadArbeidsgiver()
        soknadXml.juridiskOrganisasjonsnummer `should be equal to` "LEGAL123"

        juridiskOrgnummerRepository.deleteAll()
    }
}
