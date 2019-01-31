package no.nav.syfo.consumer.rest

import no.nav.syfo.CALL_ID
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.log
import no.nav.syfo.util.MDCOperations
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

@Component
class JuridiskLoggConsumer(private val basicAuthRestTemplate: RestTemplate,
                           @Value("\${lagrejuridisklogg.rest.url}") private val url: String,
                           @Value("\${srvsyfoaltinn.username}") private val username: String) {

    val log = log()

    fun lagreIJuriskLogg(sykepengesoknad: Sykepengesoknad): Number {

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Nav-Call-Id", MDCOperations.getFromMDC(CALL_ID))
        headers.set("Nav-Consumer-Id", username)

        val avsender = "${sykepengesoknad.aktorId} | ${sykepengesoknad.fnr}"
        val innholdMeta = "hash V5"

        val entry = Logg(
                meldingsId = sykepengesoknad.id,
                meldingsInnhold = sha512AsBase64String(innholdMeta, sykepengesoknad.xml),
                avsender = avsender,
                mottaker = sykepengesoknad.arbeidsgiver.orgnummer,
                antallAarLagres = 5
        )

        try {
            val result = basicAuthRestTemplate.exchange(url, HttpMethod.POST, HttpEntity(entry, headers), JuridiskRespose::class.java)

            if (result.statusCode != HttpStatus.OK) {
                val message = "Kall mot juridisk log feiler med HTTP-" + result.statusCode
                log.error(message)
                throw RuntimeException(message)
            }

            return result.body?.id ?: throw RuntimeException("Fikk ikke ID tilbake fra juridisk logg")
        } catch (e: HttpClientErrorException) {
            log.error("Feil ved lagring i juridisk logg", e.responseBodyAsString)
            log.error("response: " + e.responseBodyAsString)
            throw RuntimeException(e)
        }
    }
}

data class Logg(
        val meldingsId: String,
        val meldingsInnhold: String,
        val avsender: String,
        val mottaker: String,
        val joarkRef: String = "",
        val antallAarLagres: Number
)

data class JuridiskRespose(
        val id: Number
)

private fun sha512AsBase64String(metadata: String, innhold: ByteArray): String {
    try {
        val sha512 = MessageDigest.getInstance("SHA-512")
        val digest = String(sha512.digest(innhold))

        return Base64.getEncoder().encodeToString("$metadata:$digest}".toByteArray(charset("UTF-8")))
    } catch (e: NoSuchAlgorithmException) {
        throw RuntimeException("Feil ved generering av hash.", e)
    } catch (e: UnsupportedEncodingException) {
        throw RuntimeException("Feil ved generering av hash.", e)
    }
}
