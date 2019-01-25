package no.nav.syfo.config.unleash

import no.finn.unleash.DefaultUnleash
import no.finn.unleash.Unleash
import no.finn.unleash.strategy.Strategy
import no.finn.unleash.util.UnleashConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
class ToggleConfig {
    @Bean
    fun unleashConfig(
            @Value("\${unleash-api.url}") unleashApi: String,
            @Value("\${fasit.environment.name}") environment: String,
            @Value("\${app.name}") appName: String): UnleashConfig {
        return UnleashConfig.builder()
                .appName(appName)
                .instanceId(environment)
                .unleashAPI(unleashApi)
                .build()
    }

    @Bean
    @Profile("remote")
    fun unleash(
            unleashConfig: UnleashConfig,
            strategies: Array<Strategy>): Unleash {
        return DefaultUnleash(unleashConfig, *strategies)
    }
}
