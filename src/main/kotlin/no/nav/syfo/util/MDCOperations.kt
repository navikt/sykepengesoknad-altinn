package no.nav.syfo.util

import no.nav.syfo.log
import org.slf4j.MDC
import java.security.SecureRandom

/**
 * Utility-klasse for kommunikasjon med MDC.
 */
object MDCOperations {
    private val log = log()

    private val RANDOM = SecureRandom()

    private val randomNumber: Int
        get() = RANDOM.nextInt(Integer.MAX_VALUE)

    private val systemTime: Long
        get() = System.currentTimeMillis()

    fun generateCallId(): String {
        val randomNr = randomNumber
        val systemTime = systemTime

        return "NavCallId_" + systemTime + "_" + randomNr
    }

    fun getFromMDC(key: String): String? {
        val value = MDC.get(key)
        log.debug("Getting key: $key from MDC with value: $value")
        return value
    }

    fun putToMDC(key: String, value: String) {
        log.debug("Putting value: $value on MDC with key: $key")
        MDC.put(key, value)
    }

    fun remove(key: String) {
        log.debug("Removing key: $key")
        MDC.remove(key)
    }
}
