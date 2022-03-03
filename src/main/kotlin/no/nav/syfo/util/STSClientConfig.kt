package no.nav.syfo.util

import org.apache.cxf.Bus
import org.apache.cxf.binding.soap.Soap12
import org.apache.cxf.binding.soap.SoapMessage
import org.apache.cxf.endpoint.Client
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.rt.security.SecurityConstants
import org.apache.cxf.ws.policy.PolicyBuilder
import org.apache.cxf.ws.policy.PolicyEngine
import org.apache.cxf.ws.policy.attachment.reference.ReferenceResolver
import org.apache.cxf.ws.policy.attachment.reference.RemoteReferenceResolver
import org.apache.cxf.ws.security.trust.STSClient
import org.apache.neethi.Policy

internal object STSClientConfig {
    const val STS_URL_KEY = "SECURITYTOKENSERVICE_URL"
    const val SERVICEUSER_USERNAME = "SRVSYFOALTINN_USERNAME"
    const val SERVICEUSER_PASSWORD = "SRVSYFOALTINN_PASSWORD"

    // Only use no transportbinding on localhost, should use the requestSamlPolicy.xml with transport binding https
    // when in production.
    private const val STS_REQUEST_SAML_POLICY = "classpath:policy/requestSamlPolicyNoTransportBinding.xml"
    private const val STS_CLIENT_AUTHENTICATION_POLICY = "classpath:policy/untPolicy.xml"
    fun <T> configureRequestSamlToken(port: T): T {
        val client = ClientProxy.getClient(port)
        // do not have onbehalfof token so cache token in endpoint
        configureStsRequestSamlToken(client, true)
        return port
    }

    internal fun configureStsRequestSamlToken(client: Client, cacheTokenInEndpoint: Boolean) {
        // TODO: remove custom client when STS is updated to support the cxf client
        val stsClient = createCustomSTSClient(client.bus)
        configureStsWithPolicyForClient(stsClient, client, STS_REQUEST_SAML_POLICY, cacheTokenInEndpoint)
    }

    internal fun configureStsWithPolicyForClient(
        stsClient: STSClient,
        client: Client,
        policyReference: String?,
        cacheTokenInEndpoint: Boolean
    ) {
        val location = requireProperty(STS_URL_KEY)
        val username = requireProperty(SERVICEUSER_USERNAME)
        val password = requireProperty(SERVICEUSER_PASSWORD)
        configureSTSClient(stsClient, location, username, password)
        client.requestContext[SecurityConstants.STS_CLIENT] = stsClient
        client.requestContext[SecurityConstants.CACHE_ISSUED_TOKEN_IN_ENDPOINT] = cacheTokenInEndpoint
        setEndpointPolicyReference(client, policyReference)
    }

    /** Creating custom STS client because the STS on Datapower requires KeyType as a child to RequestSecurityToken and
     * TokenType as a child to SecondaryParameters. Standard CXF client put both elements in SecondaryParameters. By
     * overriding the useSecondaryParameters method you can exactly specify the request in the
     * RequestSecurityTokenTemplate in the policy.
     *
     * @param bus
     * @return
     */
    internal fun createCustomSTSClient(bus: Bus?): STSClient {
        return STSClientWSTrust13and14(bus)
    }

    internal fun configureSTSClient(
        stsClient: STSClient,
        location: String?,
        username: String,
        password: String
    ): STSClient {
        stsClient.isEnableAppliesTo = false
        stsClient.isAllowRenewing = false
        stsClient.location = location
        // For debugging
        // stsClient.setFeatures(new ArrayList<Feature>(Arrays.asList(new LoggingFeature())));
        val properties = HashMap<String, Any>()
        properties[SecurityConstants.USERNAME] = username
        properties[SecurityConstants.PASSWORD] = password
        stsClient.properties = properties

        // used for the STS client to authenticate itself to the STS provider.
        stsClient.setPolicy(STS_CLIENT_AUTHENTICATION_POLICY)
        return stsClient
    }

    internal fun setEndpointPolicyReference(client: Client, uri: String?) {
        val policy = resolvePolicyReference(client, uri)
        setClientEndpointPolicy(client, policy)
    }

    private fun requireProperty(key: String): String {
        val property = System.getenv(key)
        return property ?: systemProperty(key)
    }

    private fun systemProperty(key: String): String {
        return System.getProperty(key)
            ?: throw IllegalStateException("Required property $key not available.")
    }

    private fun resolvePolicyReference(client: Client, uri: String?): Policy {
        val policyBuilder = client.bus.getExtension(
            PolicyBuilder::class.java
        )
        val resolver: ReferenceResolver = RemoteReferenceResolver("", policyBuilder)
        return resolver.resolveReference(uri)
    }

    private fun setClientEndpointPolicy(client: Client, policy: Policy) {
        val endpoint = client.endpoint
        val endpointInfo = endpoint.endpointInfo
        val policyEngine = client.bus.getExtension(
            PolicyEngine::class.java
        )
        val message = SoapMessage(Soap12.getInstance())
        val endpointPolicy = policyEngine.getClientEndpointPolicy(endpointInfo, null, message)
        policyEngine.setClientEndpointPolicy(endpointInfo, endpointPolicy.updatePolicy(policy, message))
    }
}
