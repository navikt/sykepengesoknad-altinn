package no.nav.syfo.config;

import no.nav.syfo.consumer.ws.client.util.LogErrorHandler;
import no.nav.syfo.consumer.ws.client.util.WsClient;
import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class AktoerConfig {

    @Bean
    @SuppressWarnings("unchecked")
    public AktoerV2 aktoer(@Value("${aktoer.v2.endpointurl}") String serviceUrl) {
        return new WsClient<AktoerV2>().createPort(serviceUrl, AktoerV2.class, Collections.singletonList(new LogErrorHandler()));
    }

}
