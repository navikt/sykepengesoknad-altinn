package no.nav.syfo.config

import no.nav.tjeneste.virksomhet.organisasjon.v4.OrganisasjonV4
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OrganisasjonConfig {

    @Bean
    fun organisasjon(
        @Value("\${virksomhet.Organisasjon.v4.endpointurl}") serviceUrl: String,
    ): OrganisasjonV4 {
        return JaxWsProxyFactoryBean().apply {
            address = serviceUrl
            serviceClass = OrganisasjonV4::class.java
        }.create(OrganisasjonV4::class.java)
    }
}
