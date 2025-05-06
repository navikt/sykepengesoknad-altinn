package no.nav.syfo.client.pdf

import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate

@Configuration
class PDFClientRestTemplateConfig {
    @Bean
    fun pdfClientRestTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        restTemplate.messageConverters
            .stream()
            .filter { AbstractJackson2HttpMessageConverter::class.java.isInstance(it) }
            .map { AbstractJackson2HttpMessageConverter::class.java.cast(it) }
            .map { t: AbstractJackson2HttpMessageConverter? -> t?.objectMapper }
            .forEach { objectMapper -> objectMapper?.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false) }
        return restTemplate
    }
}
