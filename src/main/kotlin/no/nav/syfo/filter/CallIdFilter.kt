package no.nav.syfo.filter

import no.nav.syfo.kafka.NAV_CALLID
import org.slf4j.MDC
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.*
import javax.servlet.*
import javax.servlet.http.HttpServletRequest

@Component
@Order(HIGHEST_PRECEDENCE)
class CallIdFilter : Filter {

    override fun init(filterConfig: FilterConfig) {}

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        try {
            val callId = (servletRequest as? HttpServletRequest)?.getHeader(NAV_CALLID)
                ?: (servletRequest as? HttpServletRequest)?.getHeader("callId")
                ?: UUID.randomUUID().toString()

            MDC.put(NAV_CALLID, callId)

            filterChain.doFilter(servletRequest, servletResponse)
        } finally {
            MDC.remove(NAV_CALLID)
        }
    }

    override fun destroy() {}
}
