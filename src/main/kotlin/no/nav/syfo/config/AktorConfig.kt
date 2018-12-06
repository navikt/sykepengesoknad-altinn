package no.nav.syfo.config

import no.nav.syfo.consumer.ws.client.util.LogErrorHandler
import no.nav.syfo.consumer.ws.client.util.WsClient
import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AktorConfig {

    @Bean
    fun aktoer(@Value("\${aktoer.v2.endpointurl}") serviceUrl: String): AktoerV2 {
        return WsClient<AktoerV2>().createPort(serviceUrl, AktoerV2::class.java, listOf(LogErrorHandler()), true)
    }
}
