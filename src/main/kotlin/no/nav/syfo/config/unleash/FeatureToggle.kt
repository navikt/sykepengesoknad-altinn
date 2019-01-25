package no.nav.syfo.config.unleash

enum class FeatureToggle(val toggleName: String, val isAvailableInProd: Boolean) {
    ORGNUMMER_WHITELISTET("syfo.syfoaltinn.whitelist.orgnummer", false)
}
