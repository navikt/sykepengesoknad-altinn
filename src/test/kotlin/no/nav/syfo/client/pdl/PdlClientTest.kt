package no.nav.syfo.client.pdl

import no.nav.syfo.Testoppsett
import no.nav.syfo.mockPdlResponse
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldStartWith
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class PdlClientTest : Testoppsett() {

    @Autowired
    private lateinit var pdlClient: PdlClient

    final val fnr = "12345678901"

    @Test
    fun `Vi tester happycase`() {
        mockPdlResponse()

        val responseData = pdlClient.hentFormattertNavn(fnr)

        responseData `should be equal to` "Ole Gunnar"
        val request = pdlMockWebserver.takeRequest()
        request.headers["Behandlingsnummer"] `should be equal to` "B128"
        request.headers["Tema"] `should be equal to` "SYK"
        request.body.readUtf8() `should be equal to` "{\"query\":\"\\nquery(\$ident: ID!){\\n  hentPerson(ident: \$ident) {\\n  \\tnavn(historikk: false) {\\n  \\t  fornavn\\n  \\t  mellomnavn\\n  \\t  etternavn\\n    }\\n  }\\n}\\n\",\"variables\":{\"ident\":\"12345678901\"}}"

        request.headers["Authorization"]!!.shouldStartWith("Bearer ey")
    }

    @Test
    fun `Tor-Henry blir riktig kapitalisert`() {
        mockPdlResponse(
            GetPersonResponse(
                errors = emptyList(),
                data = ResponseData(
                    hentPerson = HentPerson(
                        listOf(
                            Navn(fornavn = "TOR-HENRY", etternavn = "ROARSEN", mellomnavn = null)
                        )
                    )
                )
            )
        )

        val responseData = pdlClient.hentFormattertNavn("12345")

        responseData `should be equal to` "Tor-Henry Roarsen"
        pdlMockWebserver.takeRequest()
    }

    @Test
    fun `æøå blir riktig`() {
        mockPdlResponse(
            GetPersonResponse(
                errors = emptyList(),
                data = ResponseData(
                    hentPerson = HentPerson(
                        listOf(
                            Navn(fornavn = "ÅGE", etternavn = "ÅÆØÅ", mellomnavn = "ROGER")
                        )
                    )
                )
            )
        )

        val responseData = pdlClient.hentFormattertNavn("12345")

        responseData `should be equal to` "Åge Roger Åæøå"
        pdlMockWebserver.takeRequest()
    }
}
