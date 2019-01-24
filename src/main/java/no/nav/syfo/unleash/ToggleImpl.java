package no.nav.syfo.unleash;

import no.finn.unleash.Unleash;
import no.finn.unleash.UnleashContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("remote")
@Primary
public class ToggleImpl implements Toggle {
    private Unleash unleash;
    private boolean isProd;

    public ToggleImpl(Unleash unleash,
                      @Value("${fasit.environment.name:p}") String fasitEnvironmentName) {
        this.unleash = unleash;
        isProd = "p".equals(fasitEnvironmentName);
    }

    @Override
    public boolean isEnabled(FeatureToggle toggle, UnleashContext unleashContext) {
        if (isProd && !toggle.isAvailableInProd()) {
            return false;
        }
        return unleash.isEnabled(toggle.getToggleName(), unleashContext);
    }

    @Override
    public boolean isEnabled(FeatureToggle toggle) {
        if (isProd && !toggle.isAvailableInProd()) {
            return false;
        }
        return unleash.isEnabled(toggle.getToggleName());
    }

    public boolean isProd() {
        return isProd;
    }
}
