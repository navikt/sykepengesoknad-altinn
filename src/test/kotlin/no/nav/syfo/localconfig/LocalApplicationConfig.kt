package no.nav.syfo.localconfig

import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class LocalApplicationConfig(environment: Environment) {
    /*
    Her kan du ta inn properties som normalt settes av platformen slik at de er tilgjengelige runtime lokalt
    Eks: System.setProperty("$APPLIKASJONSNAVN$_USERNAME", environment.getProperty("$APPLIKASJONSNAVN$.username"));
    */
    init {
        System.setProperty("SECURITYTOKENSERVICE_URL", environment.getProperty("securitytokenservice.url"))
        System.setProperty("SRVSYFOALTINN_USERNAME", environment.getProperty("srvsykepengesoknad-altinn.username"))
        System.setProperty("SRVSYFOALTINN_PASSWORD", environment.getProperty("srvsykepengesoknad-altinn.password"))
    }
}
