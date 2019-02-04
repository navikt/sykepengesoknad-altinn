package no.nav.syfo.consumer.rest.token

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.Objects.requireNonNull

@Component
class TokenConsumer(private val basicAuthRestTemplate: RestTemplate,
                    @Value("\${security-token-service-token.url}") private val url: String) {

    val token: Token
        get() {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

            val uriString = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("grant_type", "client_credentials")
                    .queryParam("scope", "openid")
                    .toUriString()

            val result = basicAuthRestTemplate.exchange(uriString, GET, HttpEntity<Any>(headers), Token::class.java)

            if (result.statusCode != OK) {
                throw RuntimeException("Henting av token feiler med HTTP-" + result.statusCode)
            }

            return requireNonNull<Token>(result.body)
        }
}

data class Token(
        val access_token: String,
        val token_type: String,
        val expires_in: Int
)
