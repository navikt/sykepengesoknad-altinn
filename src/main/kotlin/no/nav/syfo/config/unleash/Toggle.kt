package no.nav.syfo.config.unleash

import no.finn.unleash.UnleashContext

interface Toggle {
    val isProd: Boolean
    fun isEnabled(toggle: FeatureToggle, unleashContext: UnleashContext? = null): Boolean
}
