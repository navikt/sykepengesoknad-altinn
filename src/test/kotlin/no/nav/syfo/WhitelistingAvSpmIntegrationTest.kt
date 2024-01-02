package no.nav.syfo

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.syfo.domain.pdf.PDFSoknad
import no.nav.syfo.orgnummer.JuridiskOrgnummer
import org.amshove.kluent.*
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.*

class WhitelistingAvSpmIntegrationTest : Testoppsett() {
    @Test
    fun `Sendt arbeidstaker søknad mottas og sendes til altinn`() {
        val id = UUID.randomUUID().toString()
        val enkelSoknad = mockSykepengesoknadDTO.copy(id = id)
        val tagsPåKafka = enkelSoknad.sporsmal!!.mapNotNull { it.tag }.toSet()
        tagsPåKafka.shouldHaveSize(14)
        val sporsmalSomIkkeErWhitelistet =
            setOf(
                "YRKESSKADE_V2",
                "ANDRE_INNTEKTSKILDER_V2",
                "ANDRE_INNTEKTSKILDER",
                "MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE",
                "ARBEID_UTENFOR_NORGE",
            )
        tagsPåKafka.shouldContainAll(
            sporsmalSomIkkeErWhitelistet,
        )

        juridiskOrgnummerRepository.save(
            JuridiskOrgnummer(
                orgnummer = "12345678",
                juridiskOrgnummer = "LEGAL123",
                sykmeldingId = enkelSoknad.sykmeldingId!!,
            ),
        )
        mockPdlResponse()
        mockAltinnResponse()

        leggSøknadPåKafka(enkelSoknad)

        await().atMost(Duration.ofSeconds(10))
            .until {
                sendtSoknadRepository.existsBySykepengesoknadId(id)
            }

        pdlMockWebserver.takeRequest()
        altinnMockWebserver.takeRequest().parseCorrespondence()

        val pdfRequest: PDFSoknad = objectMapper.readValue(pdfMockWebserver.takeRequest().body.readUtf8())
        val tagsPåRequest = pdfRequest.sporsmal.mapNotNull { it.tag }.toSet()

        tagsPåRequest.shouldNotContainAny(
            sporsmalSomIkkeErWhitelistet,
        )
        tagsPåKafka.minus(tagsPåRequest).shouldBeEqualTo(sporsmalSomIkkeErWhitelistet)
        juridiskOrgnummerRepository.deleteAll()
    }
}
