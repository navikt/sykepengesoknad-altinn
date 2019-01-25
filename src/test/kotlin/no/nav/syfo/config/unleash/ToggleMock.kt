package no.nav.syfo.config.unleash

import no.finn.unleash.UnleashContext
import no.nav.syfo.config.unleash.strategy.ByEnvironmentStrategy
import no.nav.syfo.config.unleash.strategy.ByOrgnummerStrategy
import no.nav.syfo.config.unleash.strategy.UNLEASH_PARAMETER_ORGNUMRE
import org.springframework.stereotype.Service
import java.util.Collections.singletonMap

@Service
class ToggleMock(private val toggleMockConfig: ToggleMockConfig,
                 private val byEnvironmentStrategy: ByEnvironmentStrategy,
                 private val byOrgnummerStrategy: ByOrgnummerStrategy) : Toggle {

    override fun isEnabled(toggle: FeatureToggle, unleashContext: UnleashContext?): Boolean {
        val config = toggleMockConfig.config[toggle] ?: return false

        val check = listOfNotNull(
                checkEnvironment(config),
                unleashContext?.let { checkOrgnummer(config, it) }
                //Add more checks here!! N/A if null is returned!
        )
                .max()
                ?: true

        return checkEnabled(config) && check
    }

    override val isProd = false

    private fun checkEnabled(config: ToggleMockConfig.Config): Boolean {
        return config.isEnabled
    }

    private fun checkEnvironment(config: ToggleMockConfig.Config): Boolean? =
            byEnvironmentStrategy.isEnabled(singletonMap("miljø", config.byEnvironment.joinToString(",")))

    private fun checkOrgnummer(config: ToggleMockConfig.Config, unleashContext: UnleashContext): Boolean? =
            byOrgnummerStrategy.isEnabled(singletonMap(UNLEASH_PARAMETER_ORGNUMRE, config.byOrgnummer.joinToString(",")), unleashContext)
}
