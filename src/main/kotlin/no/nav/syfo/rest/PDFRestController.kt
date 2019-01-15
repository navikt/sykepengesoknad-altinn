package no.nav.syfo.rest

import no.nav.syfo.domain.soknad.Soknadsperiode
import no.nav.syfo.domain.soknad.Sporsmal
import no.nav.syfo.domain.soknad.Sykepengesoknad
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@Controller
class PDFRestController @Inject
constructor(private val restTemplate: RestTemplate) {

    fun getPDFArbeidstakere(sykepengesoknad: Sykepengesoknad): ByteArray {

        val pdfSoknad = PDFSoknad(sykepengesoknad)

        val url = "http://pdf-gen.default/api/v1/genpdf/syfosoknader/${PDFTemplate.ARBEIDSTAKERE}"

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(pdfSoknad, headers)

        val result = restTemplate.exchange(url, HttpMethod.POST, entity, ByteArray::class.java)

        if (result.statusCode != OK) {
            throw RuntimeException("getPDFArbeidstakere feiler med HTTP-" + result.statusCode + " for søknad med id: " + sykepengesoknad.id)
        }

        return result.body!!
    }

    private class PDFSoknad(sykepengesoknad: Sykepengesoknad) {
        val id: String = sykepengesoknad.id
        val fnr: String = sykepengesoknad.fnr
        val navn: String = sykepengesoknad.navn
        val sendtNav: LocalDateTime? = sykepengesoknad.sendtNav
        val sendtArbeidsgiver: LocalDateTime? = sykepengesoknad.sendtArbeidsgiver
        val sykmeldingSkrevet: LocalDateTime? = sykepengesoknad.sykmeldingSkrevet
        val arbeidsgivernavn: String? = sykepengesoknad.arbeidsgiver.navn
        val korrigerer: String? = sykepengesoknad.korrigerer
        val soknadsperioder: List<Soknadsperiode> = sykepengesoknad.soknadsperioder
        val sporsmal: List<Sporsmal> = sykepengesoknad.sporsmal
                .sortedWith(Comparator.comparingInt {
                    when (it.tag) {
                        "BEKREFT_OPPLYSNINGER", "ANSVARSERKLARING" -> 1
                        "VAER_KLAR_OVER_AT" -> 2
                        else -> 0
                    }
                })
    }

}