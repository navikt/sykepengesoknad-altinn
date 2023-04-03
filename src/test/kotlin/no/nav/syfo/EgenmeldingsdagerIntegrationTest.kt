package no.nav.syfo

import com.google.cloud.storage.Storage
import no.nav.syfo.egenmelding.egenmeldingsdager
import no.nav.syfo.model.sykmeldingstatus.ShortNameDTO
import no.nav.syfo.model.sykmeldingstatus.SporsmalOgSvarDTO
import no.nav.syfo.model.sykmeldingstatus.SvartypeDTO
import no.nav.syfo.orgnummer.JuridiskOrgnummer
import no.nav.syfo.orgnummer.skapSykmeldingKafkaMessage
import org.amshove.kluent.*
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import java.time.Duration
import java.time.LocalDate
import java.util.*

class EgenmeldingsdagerIntegrationTest : Testoppsett() {

    @Autowired
    private lateinit var storage: Storage

    @Value("\${BUCKET_NAME}")
    lateinit var bucketName: String

    @Test
    fun `Inkluderer egenmeldingsdager fra sykmelding i XML til altinn`() {
        val id = UUID.randomUUID().toString()
        val sykmeldingId = UUID.randomUUID().toString()
        val enkelSoknad = mockSykepengesoknadDTO.copy(id = id, sykmeldingId = sykmeldingId, fravar = null, egenmeldinger = null)
        juridiskOrgnummerRepository.save(
            JuridiskOrgnummer(
                orgnummer = "12345678",
                juridiskOrgnummer = "LEGAL123",
                sykmeldingId = sykmeldingId
            )
        )
        mockPdlResponse()
        mockAltinnResponse()

        leggSykmeldingPåKafka(
            skapSykmeldingKafkaMessage(
                sykmeldingId = sykmeldingId,
                orgnummer = "12345678",
                juridiskOrgnummer = "LEGAL123",
                ekstraSporsmal = listOf(
                    SporsmalOgSvarDTO(
                        tekst = "Velg dagene du brukte egenmelding",
                        shortName = ShortNameDTO.EGENMELDINGSDAGER,
                        svar = "[\"2023-03-01\",\"2023-03-10\",\"2023-03-09\",\"2023-03-13\",\"2023-03-08\"]",
                        svartype = SvartypeDTO.DAGER
                    )
                )
            )
        )
        await().atMost(Duration.ofSeconds(10))
            .until {
                egenmeldingFraSykmeldingRepository.findAll().toList().firstOrNull { it.sykmeldingId == sykmeldingId } != null
            }

        leggSøknadPåKafka(enkelSoknad)

        // Håndterer duplikat
        leggSøknadPåKafka(enkelSoknad)

        await().atMost(Duration.ofSeconds(10))
            .until {
                sendtSoknadRepository.existsBySykepengesoknadId(id)
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

        val innhold = storage.list(bucketName).values.toList()
        val relaterteFiler = innhold.filter { it.name.contains(id) }
        relaterteFiler.shouldHaveSize(6)
        relaterteFiler.first { it.name.contains("sykepengesoknad.xml") }.contentType `should be equal to` "application/xml"
        relaterteFiler.first { it.name.contains("correspondence.xml") }.contentType `should be equal to` "application/xml"
        relaterteFiler.first { it.name.contains("correspondence.gz") }.contentType `should be equal to` "application/gzip"
        relaterteFiler.first { it.name.contains("receiptExternal.xml") }.contentType `should be equal to` "application/xml"
        relaterteFiler.first { it.name.contains("receiptExternal.gz") }.contentType `should be equal to` "application/gzip"
        relaterteFiler.first { it.name.contains("sykepengesoknad.pdf") }.contentType `should be equal to` "application/pdf"

        val soknadXml = relaterteFiler.first { it.name.contains("sykepengesoknad.xml") }.getContent()
            .tilXMLSykepengesoeknadArbeidsgiver()
        soknadXml.juridiskOrganisasjonsnummer `should be equal to` "LEGAL123"
        val egenmeldingsperiodeListe = soknadXml.sykepengesoeknad.fravaer.egenmeldingsperiodeListe
        egenmeldingsperiodeListe.size `should be equal to` 3
        egenmeldingsperiodeListe[0].fom `should be equal to` LocalDate.of(2023, 3, 1)
        egenmeldingsperiodeListe[0].tom `should be equal to` LocalDate.of(2023, 3, 1)
        egenmeldingsperiodeListe[1].fom `should be equal to` LocalDate.of(2023, 3, 8)
        egenmeldingsperiodeListe[1].tom `should be equal to` LocalDate.of(2023, 3, 10)
        egenmeldingsperiodeListe[2].fom `should be equal to` LocalDate.of(2023, 3, 13)
        egenmeldingsperiodeListe[2].tom `should be equal to` LocalDate.of(2023, 3, 13)

        juridiskOrgnummerRepository.deleteAll()
    }

    @Test
    fun `Tar imot oppdateringer av egenmeldingsdager`() {
        val sykmeldingId = UUID.randomUUID().toString()

        leggSykmeldingPåKafka(
            skapSykmeldingKafkaMessage(
                sykmeldingId = sykmeldingId,
                orgnummer = "12345678",
                erSvarOppdatering = true,
                ekstraSporsmal = listOf(
                    SporsmalOgSvarDTO(
                        tekst = "Velg dagene du brukte egenmelding",
                        shortName = ShortNameDTO.EGENMELDINGSDAGER,
                        svar = "[\"2023-03-01\",\"2023-03-10\",\"2023-03-09\",\"2023-03-13\",\"2023-03-08\"]",
                        svartype = SvartypeDTO.DAGER
                    )
                )
            )
        )
        await().atMost(Duration.ofSeconds(10))
            .until {
                egenmeldingFraSykmeldingRepository.findAll().toList().firstOrNull { it.sykmeldingId == sykmeldingId }?.egenmeldingsdager()?.size == 3
            }

        leggSykmeldingPåKafka(
            skapSykmeldingKafkaMessage(
                sykmeldingId = sykmeldingId,
                orgnummer = "12345678",
                erSvarOppdatering = true,
                ekstraSporsmal = listOf(
                    SporsmalOgSvarDTO(
                        tekst = "Velg dagene du brukte egenmelding",
                        shortName = ShortNameDTO.EGENMELDINGSDAGER,
                        svar = "[\"2023-03-01\"]",
                        svartype = SvartypeDTO.DAGER
                    )
                )
            )
        )
        await().atMost(Duration.ofSeconds(10))
            .until {
                egenmeldingFraSykmeldingRepository.findAll().toList().firstOrNull { it.sykmeldingId == sykmeldingId }?.egenmeldingsdager()?.size == 1
            }

        leggSykmeldingPåKafka(
            skapSykmeldingKafkaMessage(
                sykmeldingId = sykmeldingId,
                orgnummer = "12345678",
                erSvarOppdatering = true,
                ekstraSporsmal = listOf(
                    SporsmalOgSvarDTO(
                        tekst = "Velg dagene du brukte egenmelding",
                        shortName = ShortNameDTO.EGENMELDINGSDAGER,
                        svar = "[]",
                        svartype = SvartypeDTO.DAGER
                    )
                )
            )
        )
        await().atMost(Duration.ofSeconds(10))
            .until {
                egenmeldingFraSykmeldingRepository.findAll().toList().firstOrNull { it.sykmeldingId == sykmeldingId }?.egenmeldingsdager()?.size == 0
            }
    }
}
