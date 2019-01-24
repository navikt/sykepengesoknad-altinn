package no.nav.syfo.unleash;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.Unleash;
import no.finn.unleash.strategy.Strategy;
import no.finn.unleash.util.UnleashConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
public class ToggleConfig {
    @Bean
    public UnleashConfig unleashConfig(
            @Value("${unleash-api.url}") String unleashApi,
            @Value("${fasit.environment.name}") String environment,
            @Value("${app.name}") String appName) {
        return UnleashConfig.builder()
                .appName(appName)
                .instanceId(environment)
                .unleashAPI(unleashApi)
                .build();
    }

    @Bean
    @Profile("remote")
    public Unleash unleash(
            UnleashConfig unleashConfig,
            Strategy[] strategies) {
        return new DefaultUnleash(unleashConfig, strategies);
    }
}
