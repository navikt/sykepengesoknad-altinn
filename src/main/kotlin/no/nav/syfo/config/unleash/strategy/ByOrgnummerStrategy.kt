package no.nav.syfo.config.unleash.strategy

import no.finn.unleash.UnleashContext
import no.finn.unleash.strategy.Strategy
import org.springframework.stereotype.Component

const val UNLEASH_PROPERTY_NAME_ORGNUMMER = "ORGNUMMER"
const val UNLEASH_PARAMETER_ORGNUMRE = "orgnumre"

@Component
class ByOrgnummerStrategy : Strategy {

    override fun getName(): String {
        return "byOrgnummer"
    }

    override fun isEnabled(parameters: Map<String, String>?): Boolean {
        return false
    }

    override fun isEnabled(parameters: Map<String, String>?, unleashContext: UnleashContext?): Boolean {
        val orgnummer = unleashContext?.properties?.get(UNLEASH_PROPERTY_NAME_ORGNUMMER) ?: return false

        return parameters
                ?.get(UNLEASH_PARAMETER_ORGNUMRE)
                ?.takeIf(String::isNotBlank)
                ?.let { it.split(",") }
                ?.any { it == orgnummer }
                ?: false
    }
}
