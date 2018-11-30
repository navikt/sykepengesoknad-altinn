package no.nav.syfo.config

import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternal
import no.nav.metrics.MetricsFactory.createTimerProxyForWebService
import no.nav.sbl.dialogarena.common.cxf.TimeoutFeature
import org.apache.cxf.binding.soap.SoapBindingConfiguration
import org.apache.cxf.binding.soap.SoapVersionFactory
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.ws.addressing.WSAddressingFeature
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor
import org.apache.wss4j.common.ConfigurationConstants.*
import org.apache.wss4j.common.ext.WSPasswordCallback
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.security.auth.callback.Callback
import javax.security.auth.callback.CallbackHandler

@Configuration
class AltinnConfig {

    private val RECEIVE_TIMEOUT = 10000
    private val CONNECTION_TIMEOUT = 10000

    //TODO trengs de jeg har kommentert ut?
    @Bean
    fun iCorrespondenceAgencyExternal(@Value("\${altinnUser.username}") altinnUsername: String,
                                      @Value("\${altinnUser.password}") altinnPassword: String,
                                      @Value("\${altinnServiceGateway.url}") altinnServiceGatewayUrl: String): ICorrespondenceAgencyExternal {

        val factoryBean = JaxWsProxyFactoryBean()

        factoryBean.features.add(WSAddressingFeature())
        factoryBean.features.add(TimeoutFeature(RECEIVE_TIMEOUT, CONNECTION_TIMEOUT))
        //hentIntProperty("timeout.receive", RECEIVE_TIMEOUT),
        //hentIntProperty("timeout.connection", CONNECTION_TIMEOUT)))
        //factoryBean.features.add(LoggingFeature())

        val conf = SoapBindingConfiguration()
        conf.version = SoapVersionFactory.getInstance().getSoapVersion("http://www.w3.org/2003/05/soap-envelope")

        factoryBean.bindingConfig = conf

        factoryBean.serviceClass = ICorrespondenceAgencyExternal::class.java
        factoryBean.address = altinnServiceGatewayUrl + "/ServiceEngineExternal/CorrespondenceAgencyExternal.svc"

        val map = HashMap<String, Any>()
        map[ACTION] = USERNAME_TOKEN
        map[PASSWORD_TYPE] = "PasswordText"
        map[USER] = altinnUsername

        val passwordCallbackHandler = CallbackHandler { callbacks: Array<out Callback> ->
            val callback = callbacks[0] as WSPasswordCallback
            callback.password = altinnPassword
        }

        map[PW_CALLBACK_REF] = passwordCallbackHandler
        factoryBean.outInterceptors.add(WSS4JOutInterceptor(map))

        //factoryBean.handlers.add(MDCOutHandler())

        factoryBean.create()

        return createTimerProxyForWebService(
                "Altinn.ICorrespondenceAgencyExternal",
                factoryBean.create(ICorrespondenceAgencyExternal::class.java),
                ICorrespondenceAgencyExternal::class.java)
    }

}