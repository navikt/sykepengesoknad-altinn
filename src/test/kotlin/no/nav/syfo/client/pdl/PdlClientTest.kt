package no.nav.syfo.client.pdl

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.syfo.AbstractContainerBaseTest
import no.nav.syfo.serialisertTilString
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.client.ExpectedCount.once
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.MockRestServiceServer.createServer
import org.springframework.test.web.client.RequestMatcher
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.web.client.RestTemplate
import java.net.URI

@EnableMockOAuth2Server
@DirtiesContext
class PdlClientTest : AbstractContainerBaseTest() {

    @Autowired
    private lateinit var pdlClient: PdlClient

    private lateinit var pdlMockServer: MockRestServiceServer

    @Autowired
    private lateinit var pdlRestTemplate: RestTemplate

    @BeforeEach
    fun init() {
        pdlMockServer = createServer(pdlRestTemplate)
    }

    fun harBearerToken(): RequestMatcher {
        return RequestMatcher { request: ClientHttpRequest ->

            val authHeader = request.headers.getFirst(AUTHORIZATION)
                ?: throw AssertionError("Mangler $AUTHORIZATION header")

            if (!authHeader.startsWith("Bearer ey")) {
                throw AssertionError("$AUTHORIZATION ser ikke ut til å være bearertoken")
            }
        }
    }

    @Test
    fun `Vi tester dobbelt fornavn`() {
        val getPersonResponse = GetPersonResponse(
            errors = emptyList(),
            data = ResponseData(
                hentPerson = HentPerson(
                    listOf(
                        Navn(fornavn = "ÅGE ROGER", etternavn = "ÅÆØÅ", mellomnavn = null)
                    )
                )
            )
        )

        pdlMockServer.expect(
            once(),
            requestTo(URI("http://pdl-api.pdl/graphql"))
        )
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("TEMA", "SYK"))
            .andExpect(harBearerToken())
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(getPersonResponse.serialisertTilString())
            )

        val responseData = pdlClient.hentFormattertNavn("12345")

        responseData `should be equal to` "Åge Roger Åæøå"

        pdlMockServer.verify()
    }

    @Test
    fun `Vi tester fornavn mellomnavn og etternavn`() {
        val getPersonResponse = GetPersonResponse(
            errors = emptyList(),
            data = ResponseData(
                hentPerson = HentPerson(
                    listOf(
                        Navn(fornavn = "ÅGE", etternavn = "ÅÆØÅ", mellomnavn = "ROGER")
                    )
                )
            )
        )

        pdlMockServer.expect(
            once(),
            requestTo(URI("http://pdl-api.pdl/graphql"))
        )
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("TEMA", "SYK"))
            .andExpect(harBearerToken())
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(getPersonResponse.serialisertTilString())
            )

        val responseData = pdlClient.hentFormattertNavn("12345")

        responseData `should be equal to` "Åge Roger Åæøå"

        pdlMockServer.verify()
    }

    @Test
    fun `Tor-Henry blir riktig kapitalisert`() {
        val getPersonResponse = GetPersonResponse(
            errors = emptyList(),
            data = ResponseData(
                hentPerson = HentPerson(
                    listOf(
                        Navn(fornavn = "TOR-HENRY", etternavn = "ROARSEN", mellomnavn = null)
                    )
                )
            )
        )

        pdlMockServer.expect(
            once(),
            requestTo(URI("http://pdl-api.pdl/graphql"))
        )
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("TEMA", "SYK"))
            .andExpect(harBearerToken())
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(getPersonResponse.serialisertTilString())
            )

        val responseData = pdlClient.hentFormattertNavn("12345")

        responseData `should be equal to` "Tor-Henry Roarsen"

        pdlMockServer.verify()
    }
}
