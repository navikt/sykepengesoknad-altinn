package no.nav.syfo.config.unleash

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "toggle")
class ToggleMockConfig {
    var config: MutableMap<FeatureToggle, Config> = mutableMapOf()

    class Config {
        var isEnabled: Boolean = false
        var byEnvironment: List<String> = mutableListOf()
        var byOrgnummer: List<String> = mutableListOf()
    }
}
