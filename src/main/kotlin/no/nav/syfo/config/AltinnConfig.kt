package no.nav.syfo.config

import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasic
import no.nav.syfo.util.LogErrorHandler
import no.nav.syfo.util.WsClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AltinnConfig {

    @Bean
    fun iCorrespondenceAgencyExternalBasic(
        @Value("\${ekstern.altinn.behandlealtinnmelding.v1.endpointurl}") behandleAltinnMeldingUrl: String,
        @Value("\${ws.sts.enabled:true}") wsStsEnabled: Boolean
    ): ICorrespondenceAgencyExternalBasic {
        return WsClient<ICorrespondenceAgencyExternalBasic>().createPort(
            behandleAltinnMeldingUrl, ICorrespondenceAgencyExternalBasic::class.java,
            listOf(
                LogErrorHandler()
            ),
            false, wsStsEnabled
        )
    }
}
