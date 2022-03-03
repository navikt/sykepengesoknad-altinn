package no.nav.syfo.util

import org.apache.cxf.Bus
import org.apache.cxf.ws.security.tokenstore.SecurityToken
import org.apache.cxf.ws.security.trust.STSClient

class STSClientWSTrust13and14(b: Bus?) : STSClient(b) {
    /** Only here to allow to use elements for both WS-Trust 1.3 and 1.4 in the request, as the STS implemented on
     * Datapower requires the use of KeyType directly as child to RequestSecurityToken even if you use
     * SecondaryParameters.
     *
     * Setting this to false should allow you to specify a RequestSecurityTokenTemplate with SecondaryParameters in
     * policy attachment, at the same time as KeyType is specified as a child to RequestSecurityToken.  */
    override fun useSecondaryParameters(): Boolean {
        return false
    }

    @Throws(Exception::class)
    override fun requestSecurityToken(
        appliesTo: String,
        action: String,
        requestType: String,
        binaryExchange: String
    ): SecurityToken {
        return super.requestSecurityToken(appliesTo, action, requestType, binaryExchange)
    }
}
