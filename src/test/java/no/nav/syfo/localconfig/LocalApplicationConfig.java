package no.nav.syfo.localconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class LocalApplicationConfig {

    public LocalApplicationConfig(Environment environment) {
        /*
            Her kan du ta inn properties som normalt settes av platformen slik at de er tilgjengelige runtime lokalt
            Eks: System.setProperty("$APPLIKASJONSNAVN$_USERNAME", environment.getProperty("$APPLIKASJONSNAVN$.username"));
         */
        System.setProperty("SECURITYTOKENSERVICE_URL", environment.getProperty("securitytokenservice.url"));
        System.setProperty("SRVSYFOSPRINGBOOT_USERNAME", environment.getProperty("srvSyfospringboot.username"));
        System.setProperty("SRVSYFOSPRINGBOOT_PASSWORD", environment.getProperty("srvSyfospringboot.password"));
    }
}
