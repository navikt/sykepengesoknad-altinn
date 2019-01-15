package no.nav.syfo.rest

import no.nav.syfo.domain.soknad.Soknadsperiode
import no.nav.syfo.domain.soknad.Sykepengesoknad
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.client.RestTemplate
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
        val soknadId = sykepengesoknad.id
        val fnr = sykepengesoknad.fnr
        val navn = sykepengesoknad.navn
        val innsendtDato = sykepengesoknad.sendtNav?.toLocalDate()
        val sendtArbeidsgiver = sykepengesoknad.sendtArbeidsgiver?.toLocalDate()
        val sykmeldingUtskrevet = sykepengesoknad.sykmeldingSkrevet?.toLocalDate()
        val arbeidsgiver = sykepengesoknad.arbeidsgiver.navn
        val korrigerer = sykepengesoknad.korrigerer
        val soknadPerioder = sykepengesoknad.soknadsperioder.map { PDFPeriode(it) }
        val sporsmal = sykepengesoknad.sporsmal
                .sortedWith(Comparator.comparingInt {
                    when (it.tag) {
                        "BEKREFT_OPPLYSNINGER", "ANSVARSERKLARING" -> 1
                        "VAER_KLAR_OVER_AT" -> 2
                        else -> 0
                    }
                })
    }

    private class PDFPeriode(soknadsperiode: Soknadsperiode) {
        val fom = soknadsperiode.fom
        val tom = soknadsperiode.tom
        val grad = soknadsperiode.sykmeldingsgrad
    }

}