package no.nav.syfo.consumer.rest

import no.nav.syfo.LocalApplication
import no.nav.syfo.consumer.rest.aktor.Aktor
import no.nav.syfo.consumer.rest.aktor.AktorResponse
import no.nav.syfo.consumer.rest.aktor.AktorRestConsumer
import no.nav.syfo.consumer.rest.aktor.Ident
import no.nav.syfo.consumer.rest.token.Token
import no.nav.syfo.consumer.rest.token.TokenConsumer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.*
import org.mockito.Mock
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.web.client.RestTemplate
import javax.inject.Inject

@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
@EmbeddedKafka
class AktorRestConsumerTest {

    @Mock
    lateinit var tokenConsumer: TokenConsumer

    @Mock
    lateinit var restTemplate: RestTemplate

    @Inject
    private lateinit var aktorConsumer: AktorRestConsumer

    @BeforeEach
    fun setup() {
        aktorConsumer = AktorRestConsumer(
            tokenConsumer = tokenConsumer,
            username = "username",
            url = "https://aktor.nav.no",
            restTemplate = restTemplate
        )
        given(tokenConsumer.token).willReturn(Token("token", "Bearer", 3600))
    }

    @Test
    fun finnerAktorId() {
        val response = AktorResponse()
        response["fnr"] = Aktor(
            identer = listOf(
                Ident(
                    ident = "aktorId",
                    identgruppe = "AktoerId",
                    gjeldende = true
                )
            ),
            feilmelding = null
        )

        given(
            restTemplate.exchange(
                anyString(),
                any(HttpMethod::class.java),
                any(HttpEntity::class.java),
                eq(AktorResponse::class.java)
            )
        ).willReturn(ResponseEntity(response, HttpStatus.OK))

        val aktorId = aktorConsumer.getAktorId("fnr")

        assertThat(aktorId).isEqualTo("aktorId")
    }

    @Test
    fun finnerIkkeIdent() {
        assertThrows(RuntimeException::class.java) {
            val response = AktorResponse()
            response["fnr"] = Aktor(
                identer = null,
                feilmelding = "Fant ikke aktor"
            )

            given(
                restTemplate.exchange(
                    anyString(),
                    any(HttpMethod::class.java),
                    any(HttpEntity::class.java),
                    eq(AktorResponse::class.java)
                )
            ).willReturn(ResponseEntity(response, HttpStatus.OK))

            aktorConsumer.getAktorId("fnr")
        }
    }

    @Test
    fun manglendeFnrIResponseGirFeilmelding() {

        assertThrows(RuntimeException::class.java) {
            val response = AktorResponse()
            response["etAnnetFnr"] = Aktor(
                identer = listOf(
                    Ident(
                        ident = "aktorId",
                        identgruppe = "AktoerId",
                        gjeldende = true
                    )
                ),
                feilmelding = null
            )

            given(
                restTemplate.exchange(
                    anyString(),
                    any(HttpMethod::class.java),
                    any(HttpEntity::class.java),
                    eq(AktorResponse::class.java)
                )
            ).willReturn(ResponseEntity(response, HttpStatus.OK))

            aktorConsumer.getAktorId("fnr")
        }
    }

    @Test
    fun manglendeIdentGirFeilmelding() {
        assertThrows(RuntimeException::class.java) {
            val response = AktorResponse()
            response["fnr"] = Aktor(
                identer = emptyList(),
                feilmelding = null
            )

            given(
                restTemplate.exchange(
                    anyString(),
                    any(HttpMethod::class.java),
                    any(HttpEntity::class.java),
                    eq(AktorResponse::class.java)
                )
            ).willReturn(ResponseEntity(response, HttpStatus.OK))

            aktorConsumer.getAktorId("fnr")
        }
    }
}
