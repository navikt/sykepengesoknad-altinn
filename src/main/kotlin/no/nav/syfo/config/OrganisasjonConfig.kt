package no.nav.syfo.config

import no.nav.syfo.consumer.ws.client.util.LogErrorHandler
import no.nav.syfo.consumer.ws.client.util.WsClient
import no.nav.tjeneste.virksomhet.organisasjon.v4.OrganisasjonV4
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class OrganisasjonConfig {

    @Bean
    fun organisasjon(@Value("\${virksomhet.Organisasjon.v4.endpointurl}") serviceUrl: String,
                     @Value("\${ws.sts.enabled:true}") wsStsEnabled: Boolean): OrganisasjonV4 {
        return WsClient<OrganisasjonV4>().createPort(serviceUrl, OrganisasjonV4::class.java, listOf(LogErrorHandler()), true, wsStsEnabled)
    }

}
