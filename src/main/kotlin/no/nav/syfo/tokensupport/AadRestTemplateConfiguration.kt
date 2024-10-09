package no.nav.syfo.tokensupport

import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.core5.util.Timeout
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.time.Duration
import java.util.function.Supplier

@EnableOAuth2Client(cacheEnabled = true)
@Configuration
@EnableJwtTokenValidation
class AadRestTemplateConfiguration {


    @Bean
    fun httpClient(): CloseableHttpClient {
        val connectionManager = PoolingHttpClientConnectionManager()
        connectionManager.defaultMaxPerRoute = 50
        connectionManager.maxTotal = 50

        return HttpClientBuilder.create()
            .setConnectionManager(connectionManager)
            .evictExpiredConnections()
            .evictIdleConnections(Timeout.ofMinutes(1))
            .build()
    }

    @Bean
    fun pdlRestTemplate(
        restTemplateBuilder: RestTemplateBuilder,
        clientConfigurationProperties: ClientConfigurationProperties,
        oAuth2AccessTokenService: OAuth2AccessTokenService,
        httpClient: CloseableHttpClient
    ): RestTemplate =
        downstreamRestTemplate(
            registrationName = "pdl-api-client-credentials",
            restTemplateBuilder = restTemplateBuilder,
            clientConfigurationProperties = clientConfigurationProperties,
            oAuth2AccessTokenService = oAuth2AccessTokenService,
            httpClient = httpClient
        )

    private fun downstreamRestTemplate(
        restTemplateBuilder: RestTemplateBuilder,
        clientConfigurationProperties: ClientConfigurationProperties,
        oAuth2AccessTokenService: OAuth2AccessTokenService,
        registrationName: String,
        httpClient: CloseableHttpClient,
        connectTimeout: Duration = Duration.ofSeconds(2),
        readTimeout: Duration = Duration.ofSeconds(3),
    ): RestTemplate {
        val clientProperties =
            clientConfigurationProperties.registration[registrationName]
                ?: throw RuntimeException("Fant ikke config for $registrationName")

        val requestFactory = HttpComponentsClientHttpRequestFactory(httpClient)

        return restTemplateBuilder
            .requestFactory(Supplier { requestFactory })
            .setConnectTimeout(connectTimeout)
            .setReadTimeout(readTimeout)
            .additionalInterceptors(bearerTokenInterceptor(clientProperties, oAuth2AccessTokenService))
            .build()
    }

    private fun bearerTokenInterceptor(
        clientProperties: ClientProperties,
        oAuth2AccessTokenService: OAuth2AccessTokenService,
    ): ClientHttpRequestInterceptor {
        return ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution ->
            val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
            response.accessToken?.let { request.headers.setBearerAuth(it) }
            execution.execute(request, body)
        }
    }
}
