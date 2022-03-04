package no.nav.syfo.config

import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasic
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AltinnConfig {

    @Bean
    fun iCorrespondenceAgencyExternalBasic(@Value("\${altinn.url}") altinnUrl: String): ICorrespondenceAgencyExternalBasic {
        return JaxWsProxyFactoryBean().apply {
            address = "$altinnUrl/ServiceEngineExternal/CorrespondenceAgencyExternalBasic.svc"
            serviceClass = ICorrespondenceAgencyExternalBasic::class.java
        }.create(ICorrespondenceAgencyExternalBasic::class.java)
    }
}
