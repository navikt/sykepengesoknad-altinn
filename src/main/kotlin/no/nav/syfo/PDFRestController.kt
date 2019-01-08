package no.nav.syfo

import no.nav.syfo.domain.soknad.Sykepengesoknad
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.client.RestTemplate
import javax.inject.Inject

@Controller
class PDFRestController @Inject
constructor(private val restTemplate: RestTemplate) {

    //TODO soknad på riktig format til pdfgen
    fun getPDF(soknad: Sykepengesoknad, template: PDFTemplate): ByteArray {

        val url = "http://pdf-gen.default/api/v1/genpdf/syfosoknader/$template"

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(soknad, headers)

        val result = restTemplate.exchange(url, HttpMethod.POST, entity, ByteArray::class.java)

        if (result.statusCode != OK) {
            throw RuntimeException("getPDF feiler med HTTP-" + result.statusCode + " for søknad om utenlandsopphold med id: " + soknad.id)
        }

        return result.body!!
    }


}