package no.nav.syfo.unleash;

public enum FeatureToggle {
    ORGNUMMER_WHITELISTET("syfo.syfoaltinn.whitelist.orgnummer", false);

    private final String toggleName;
    private final boolean availableInProd;

    FeatureToggle(String toggleName, boolean availableInProd) {
        this.toggleName = toggleName;
        this.availableInProd = availableInProd;
    }

    public String getToggleName() {
        return toggleName;
    }

    public boolean isAvailableInProd() {
        return availableInProd;
    }
}
