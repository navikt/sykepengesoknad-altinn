package no.nav.syfo.consumer.rest.aktor

import no.nav.syfo.CALL_ID
import no.nav.syfo.consumer.rest.aktor.AktorResponse
import no.nav.syfo.log
import no.nav.syfo.token.TokenConsumer
import no.nav.syfo.util.MDCOperations.getFromMDC
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

    private fun getIdent(sokeIdent: String, identgruppe: String): String {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.set("Authorization", "Bearer " + tokenConsumer.token.access_token)
        headers.set("Nav-Call-Id", getFromMDC(CALL_ID))
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

