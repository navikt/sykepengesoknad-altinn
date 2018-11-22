package no.nav.syfo.localconfig

import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class LocalApplicationConfig(environment: Environment)/*
            Her kan du ta inn properties som normalt settes av platformen slik at de er tilgjengelige runtime lokalt
            Eks: System.setProperty("syfoaltinn_USERNAME", environment.getProperty("syfoaltinn.username"));
         */
