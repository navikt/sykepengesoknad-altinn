package no.nav.syfo.unleash;

import no.finn.unleash.UnleashContext;

public interface Toggle {
    boolean isEnabled(FeatureToggle toggle, UnleashContext unleashContext);
    boolean isEnabled(FeatureToggle toggle);
    boolean isProd();
}
