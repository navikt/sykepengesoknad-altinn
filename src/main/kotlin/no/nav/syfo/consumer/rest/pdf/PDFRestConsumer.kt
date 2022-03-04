package no.nav.syfo.consumer.rest.pdf

import no.nav.syfo.domain.pdf.PDFSoknad
import no.nav.syfo.domain.soknad.Soknadstype
import no.nav.syfo.domain.soknad.Sykepengesoknad
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class PDFRestConsumer(
    private val pdfClientRestTemplate: RestTemplate,
    @Value("\${pdfgen.url}") private val pdfgenUrl: String
) {

    @Retryable(backoff = Backoff(delay = 5000))
    fun getPDF(sykepengesoknad: Sykepengesoknad, fnr: String, navn: String): ByteArray {

        val pdfSoknad = PDFSoknad(sykepengesoknad, fnr, navn)

        val url = when (sykepengesoknad.type) {
            Soknadstype.ARBEIDSTAKERE -> "$pdfgenUrl/api/v1/genpdf/syfosoknader/${PDFTemplate.ARBEIDSTAKERE}"
            Soknadstype.BEHANDLINGSDAGER -> "$pdfgenUrl/api/v1/genpdf/syfosoknader/${PDFTemplate.BEHANDLINGSDAGER}"
            Soknadstype.GRADERT_REISETILSKUDD -> "$pdfgenUrl/api/v1/genpdf/syfosoknader/${PDFTemplate.GRADERTREISETILSKUDD}"
            else -> throw RuntimeException("Har ikke implementert PDF-template for søknad av typen: ${sykepengesoknad.type}")
        }

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(pdfSoknad, headers)

        val result = pdfClientRestTemplate.exchange(url, HttpMethod.POST, entity, ByteArray::class.java)

        if (result.statusCode != OK) {
            throw RuntimeException("getPDFArbeidstakere feiler med HTTP-" + result.statusCode + " for søknad med id: " + sykepengesoknad.id)
        }

        return result.body ?: throw RuntimeException("pdfgen returnerer null - dette er en feil")
    }
}
