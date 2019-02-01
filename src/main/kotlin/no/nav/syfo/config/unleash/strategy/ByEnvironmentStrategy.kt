package no.nav.syfo.config.unleash.strategy

import no.finn.unleash.strategy.Strategy
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ByEnvironmentStrategy(@Value("\${fasit.environment.name}") private val environment: String) : Strategy {

    override fun getName(): String {
        return "byEnvironment"
    }

    override fun isEnabled(parameters: Map<String, String>?): Boolean {
        val envKey = "miljø"

        return parameters
                ?.get(envKey)
                ?.takeIf(String::isNotBlank)
                ?.let { it.split(",") }
                ?.map(String::trim)
                ?.any { it == environment }
                ?: false
    }
}
