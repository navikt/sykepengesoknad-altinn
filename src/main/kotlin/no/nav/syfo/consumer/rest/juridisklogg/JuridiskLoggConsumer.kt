package no.nav.syfo.consumer.rest.juridisklogg

import no.nav.syfo.domain.AltinnInnsendelseEkstraData
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

@Component
class JuridiskLoggConsumer(
    private val basicAuthRestTemplate: RestTemplate,
    @Value("\${lagrejuridisklogg.rest.url}") private val url: String,
    @Value("\${srvsyfoaltinn.username}") private val username: String
) {

    val log = logger()

    @Retryable(backoff = Backoff(delay = 5000))
    fun lagreIJuridiskLogg(sykepengesoknad: Sykepengesoknad, altinnKvitteringsId: Number, ekstraData: AltinnInnsendelseEkstraData): Number {

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Nav-Call-Id", sykepengesoknad.id)
        headers.set("Nav-Consumer-Id", username)

        val avsender = sykepengesoknad.fnr
        val innholdMeta = "hash: V6;altinnKvittering: $altinnKvitteringsId"
        val entry = sha512AsBase64String(innholdMeta, ekstraData.xml)

        val logg = Logg(
            meldingsId = sykepengesoknad.id,
            meldingsInnhold = entry,
            avsender = avsender,
            mottaker = sykepengesoknad.arbeidsgiver.orgnummer,
            antallAarLagres = 5
        )

        try {
            val result = basicAuthRestTemplate.exchange(url, HttpMethod.POST, HttpEntity(logg, headers), JuridiskRespose::class.java)

            if (result.statusCode != HttpStatus.OK) {
                log.error(
                    "Kall mot juridisk log feiler med HTTP-${result.statusCode}\n" +
                        "Payload: $entry"
                )
                throw JuridiskLoggException(message = "Kall mot juridisk log feiler med HTTP-${result.statusCode}")
            }

            return result.body?.id ?: throw JuridiskLoggException("Fikk ikke ID tilbake fra juridisk logg")
        } catch (e: HttpClientErrorException) {
            log.error(
                "Feil ved lagring i juridisk logg: ${e.responseBodyAsString}\n" +
                    "Payload: $entry"
            )
            throw JuridiskLoggException("Feil ved lagring i juridisk logg", e)
        }
    }
}

class JuridiskLoggException(message: String? = "", cause: Throwable? = null) : RuntimeException(message, cause)

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
        val digest = String(sha512.digest(innhold), UTF_8)

        return Base64.getEncoder().encodeToString("$metadata:$digest}".toByteArray(UTF_8))
    } catch (e: NoSuchAlgorithmException) {
        throw RuntimeException("Feil ved generering av hash.", e)
    } catch (e: UnsupportedEncodingException) {
        throw RuntimeException("Feil ved generering av hash.", e)
    }
}
