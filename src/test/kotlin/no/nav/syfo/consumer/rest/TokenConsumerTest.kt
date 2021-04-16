package no.nav.syfo.consumer.rest

import no.nav.syfo.LocalApplication
import no.nav.syfo.consumer.rest.token.Token
import no.nav.syfo.consumer.rest.token.TokenConsumer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.web.client.RestTemplate

@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
@EmbeddedKafka
class TokenConsumerTest {
    @Mock
    lateinit var restTemplate: RestTemplate

    @Autowired
    private lateinit var tokenConsumer: TokenConsumer

    private val token = Token("token", "Bearer", 3600)

    @BeforeEach
    fun setup() {
        tokenConsumer = TokenConsumer(
                restTemplate,
                url = "https://url.no"
        )
    }

    @Test
    fun tokenLeggesICache() {
        mockTokenResponse(token)
        val hentingNr1 = tokenConsumer.hentToken()
        assertThat(hentingNr1.access_token).isEqualTo("token")

        mockTokenResponse(token.copy(access_token = "token2"))
        val hentingNr2 = tokenConsumer.hentToken()
        assertThat(hentingNr2).isEqualTo(hentingNr1)
    }

    @Test
    fun tokenFornyes() {
        mockTokenResponse(token.copy(expires_in = 0))
        val hentingNr1 = tokenConsumer.hentToken()
        assertThat(hentingNr1.access_token).isEqualTo("token")

        mockTokenResponse(token.copy(access_token = "token2"))
        val hentingNr2 = tokenConsumer.hentToken()
        assertThat(hentingNr2).isNotEqualTo(hentingNr1)
        assertThat(hentingNr2.access_token).isEqualTo("token2")
    }

    private fun mockTokenResponse(token: Token) {
        BDDMockito.given(restTemplate.exchange(
                BDDMockito.anyString(),
                BDDMockito.any(HttpMethod::class.java),
                BDDMockito.any(HttpEntity::class.java),
                BDDMockito.eq(Token::class.java)
        )).willReturn(ResponseEntity(token, HttpStatus.OK))
    }
}
