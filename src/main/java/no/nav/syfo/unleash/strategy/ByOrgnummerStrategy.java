package no.nav.syfo.unleash.strategy;

import no.finn.unleash.UnleashContext;
import no.finn.unleash.strategy.Strategy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

import static java.util.Optional.ofNullable;

@Component
public class ByOrgnummerStrategy implements Strategy {

    public static final String UNLEASH_PROPERTY_NAME_ORGNUMMER = "ORGNUMMER";
    private static final String UNLEASH_PARAMETER_ORGNUMRE = "orgnumre";

    @Override
    public String getName() {
        return "byOrgnummer";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return false;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        final String orgnummer = unleashContext.getProperties().get(UNLEASH_PROPERTY_NAME_ORGNUMMER);
        return ofNullable(parameters.get(UNLEASH_PARAMETER_ORGNUMRE))
                .filter(parameter -> !parameter.isEmpty())
                .map(parameter -> parameter.split(","))
                .map(Arrays::stream)
                .filter(orgnumre -> orgnumre.anyMatch(orgnummer::equals))
                .isPresent();
    }


}
