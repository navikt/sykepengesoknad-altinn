package no.nav.syfo.config

import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate

@Configuration
class ApplicationConfig {
    @Bean
    fun restTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        restTemplate.messageConverters.stream()
                .filter { AbstractJackson2HttpMessageConverter::class.java.isInstance(it) }
                .map { AbstractJackson2HttpMessageConverter::class.java.cast(it) }
                .map { t: AbstractJackson2HttpMessageConverter? -> t?.objectMapper }
                .forEach { objectMapper -> objectMapper?.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false) }
        return restTemplate
    }

    @Bean
    fun basicAuthRestTemplate(@Value("\${srvsyfoaltinn.username}") username: String,
                              @Value("\${srvsyfoaltinn.password}") password: String): RestTemplate {
        return RestTemplateBuilder()
                .basicAuthorization(username, password)
                .build()
    }
}
