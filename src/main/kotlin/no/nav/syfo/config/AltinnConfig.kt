package no.nav.syfo.config

import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasic
import no.nav.syfo.consumer.ws.client.util.LogErrorHandler
import no.nav.syfo.consumer.ws.client.util.WsClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AltinnConfig {

    @Bean
    fun iCorrespondenceAgencyExternalBasic(@Value("\${behandlealtinnmelding.endpoint.url}") behandleAltinnMeldingUrl: String): ICorrespondenceAgencyExternalBasic {
        return WsClient<ICorrespondenceAgencyExternalBasic>().createPort(behandleAltinnMeldingUrl, ICorrespondenceAgencyExternalBasic::class.java, listOf(LogErrorHandler()))
    }

}