package no.nav.syfo.consumer.ws.client.util;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptor;
import org.apache.cxf.ws.addressing.WSAddressingFeature;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class WsClient<T> {

    @SuppressWarnings("unchecked")
    public T createPort(String serviceUrl,
                        Class<?> portType,
                        List<Handler> handlers,
                        boolean inkluderWSAddressing,
                        boolean wsStsEnabled,
                        PhaseInterceptor<? extends Message>... interceptors) {
        JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
        jaxWsProxyFactoryBean.setServiceClass(portType);
        jaxWsProxyFactoryBean.setAddress(Objects.requireNonNull(serviceUrl));
        if (inkluderWSAddressing) {
            jaxWsProxyFactoryBean.getFeatures().add(new WSAddressingFeature());
        }
        T port = (T) jaxWsProxyFactoryBean.create();
        if (wsStsEnabled) {
            ((BindingProvider) port).getBinding().setHandlerChain(handlers);
            Client client = ClientProxy.getClient(port);
            Arrays.stream(interceptors).forEach(client.getOutInterceptors()::add);
            STSClientConfig.configureRequestSamlToken(port);
        }
        return port;
    }

}
