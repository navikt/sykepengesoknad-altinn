package no.nav.syfo.config.unleash

import no.finn.unleash.Unleash
import no.finn.unleash.UnleashContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("remote")
@Primary
class ToggleImpl(private val unleash: Unleash,
                 @Value("\${nais.cluster.name:prod-fss}") clusterName: String) : Toggle {
    override val isProd: Boolean = "prod-fss" == clusterName

    override fun isEnabled(toggle: FeatureToggle, unleashContext: UnleashContext?): Boolean =
            when {
                (isProd && !toggle.isAvailableInProd) -> false
                unleashContext != null -> unleash.isEnabled(toggle.toggleName, unleashContext)
                else -> unleash.isEnabled(toggle.toggleName)
            }
}
