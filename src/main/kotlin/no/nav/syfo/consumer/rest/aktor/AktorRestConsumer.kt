package no.nav.syfo.consumer.rest.aktor

import no.nav.syfo.CALL_ID
import no.nav.syfo.consumer.rest.token.TokenConsumer
import no.nav.syfo.log
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

class AktorResponse : HashMap<String, Aktor>()
data class Aktor(val identer: List<Ident>? = null, val feilmelding: String? = null)
data class Ident(val ident: String, val identgruppe: String, val gjeldende: Boolean)

@Component
class AktorRestConsumer(private val tokenConsumer: TokenConsumer,
                        @Value("\${srvsyfoaltinn.username}") private val username: String,
                        @Value("\${aktoerregister.api.v1.url}") private val url: String,
                        private val restTemplate: RestTemplate) {

    val log = log()

    fun getAktorId(fnr: String): String {
        return getIdent(fnr, "AktoerId")
    }

    fun getFnr(aktorId: String): String {
        return getIdent(aktorId, "NorskIdent")
    }

    private fun callId(): String {
        val callId = MDC.get(CALL_ID)
        return if (callId.isNullOrEmpty()) {
            UUID.randomUUID().toString()
        } else {
            callId
        }
    }

    private fun getIdent(sokeIdent: String, identgruppe: String): String {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.set("Authorization", "Bearer " + tokenConsumer.token.access_token)
        headers.set("Nav-Call-Id", callId())
        headers.set("Nav-Consumer-Id", username)
        headers.set("Nav-Personidenter", sokeIdent)

        val uriString = UriComponentsBuilder
                .fromHttpUrl("$url/identer")
                .queryParam("gjeldende", "true")
                .queryParam("identgruppe", identgruppe)
                .toUriString()
        try {
            val result = restTemplate
                    .exchange(uriString, GET, HttpEntity<Any>(headers), AktorResponse::class.java)

            if (result.statusCode != OK) {
                val message = "Kall mot aktørregister feiler med HTTP-" + result.statusCode
                log.error(message)
                throw RuntimeException(message)
            }

            return result
                    .body
                    ?.get(sokeIdent)
                    .let { aktor ->
                        aktor?.identer ?: throw RuntimeException("Fant ikke aktøren: " + aktor?.feilmelding)
                    }
                    .filter { ident -> ident.gjeldende }
                    .map { ident -> ident.ident }
                    .first()

        } catch (e: HttpClientErrorException) {
            log.error("Feil ved oppslag i aktørtjenesten", e)
            throw RuntimeException(e)
        }
    }
}
