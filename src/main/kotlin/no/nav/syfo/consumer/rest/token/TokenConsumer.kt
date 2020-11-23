package no.nav.syfo.consumer.rest.token

import no.nav.syfo.consumer.rest.token.Token.Companion.shouldRenewToken
import no.nav.syfo.log
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.time.Instant
import java.util.Objects.requireNonNull

@Component
class TokenConsumer(private val basicAuthRestTemplate: RestTemplate,
                    @Value("\${security.token.service.token.url}") private val url: String) {

    private val log = log()
    private var cachedToken: Token? = null
    val token: Token
        get() {
            return hentToken()
        }

    fun hentToken(): Token {
        if (shouldRenewToken(cachedToken)) {
            log.info("Henter nytt token fra STS")

            val result = basicAuthRestTemplate.exchange(uriString(), GET, headers(), Token::class.java)

            if (result.statusCode != OK) {
                throw RuntimeException("Henting av token feiler med HTTP-" + result.statusCode)
            }

            cachedToken = result.body
        }

        return requireNonNull<Token>(cachedToken)
    }

    private fun uriString(): String {
        return UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("grant_type", "client_credentials")
                .queryParam("scope", "openid")
                .toUriString()
    }

    private fun headers(): HttpEntity<Any> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        return HttpEntity<Any>(headers)
    }
}

data class Token(
        val access_token: String,
        val token_type: String,
        val expires_in: Int
) {

    val expirationTime: Instant = Instant.now().plusSeconds(expires_in - 10L)

    companion object {
        fun shouldRenewToken(token: Token?): Boolean {
            if (token == null) {
                return true
            }
            return isExpired(token)
        }

        private fun isExpired(token: Token): Boolean {
            return token.expirationTime.isBefore(Instant.now())
        }
    }
}
