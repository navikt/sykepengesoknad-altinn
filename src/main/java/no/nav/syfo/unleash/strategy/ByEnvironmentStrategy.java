package no.nav.syfo.unleash.strategy;

import no.finn.unleash.strategy.Strategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

import static java.util.Optional.ofNullable;

@Component
public class ByEnvironmentStrategy implements Strategy {
    private final String environment;

    public ByEnvironmentStrategy(@Value("${fasit.environment.name}") String environment) {
        this.environment = environment;
    }

    @Override
    public String getName() {
        return "byEnvironment";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        String envKey = "miljø";
        return ofNullable(parameters)
                .filter(map -> map.containsKey(envKey))
                .map(map -> map.get(envKey))
                .map(envs -> envs.split(","))
                .map(Arrays::stream)
                .filter(envs -> envs.map(String::trim).anyMatch(environment::equals))
                .isPresent();
    }
}
