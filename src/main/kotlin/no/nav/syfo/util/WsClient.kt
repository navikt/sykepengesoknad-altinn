package no.nav.syfo.util

import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.message.Message
import org.apache.cxf.phase.PhaseInterceptor
import org.apache.cxf.ws.addressing.WSAddressingFeature
import java.util.*
import javax.xml.ws.BindingProvider
import javax.xml.ws.handler.Handler

@Suppress("UNCHECKED_CAST")
class WsClient<T> {
    fun createPort(
        serviceUrl: String,
        portType: Class<*>?,
        handlers: List<Handler<*>?>?,
        inkluderWSAddressing: Boolean,
        wsStsEnabled: Boolean,
        vararg interceptors: PhaseInterceptor<out Message>
    ): T {
        val jaxWsProxyFactoryBean = JaxWsProxyFactoryBean()
        jaxWsProxyFactoryBean.serviceClass = portType
        jaxWsProxyFactoryBean.address = Objects.requireNonNull(serviceUrl)
        if (inkluderWSAddressing) {
            jaxWsProxyFactoryBean.features.add(WSAddressingFeature())
        }
        val port = jaxWsProxyFactoryBean.create() as T
        if (wsStsEnabled) {
            (port as BindingProvider).binding.handlerChain = handlers
            val client = ClientProxy.getClient(port)
            Arrays.stream(interceptors).forEach { e: PhaseInterceptor<out Message>? -> client.outInterceptors.add(e) }
            STSClientConfig.configureRequestSamlToken<T>(port)
        }
        return port
    }
}
