package no.nav.syfo.config

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RestTemplateBuilderConfig {
    @Bean
    fun restTemplateBuilder(): RestTemplateBuilder {
        // Customizer resttemplatebuilder som blir brukt i OAuth2ClientConfiguration i token support
        return RestTemplateBuilder()
            .additionalCustomizers(NaisProxyCustomizer())
    }
}
