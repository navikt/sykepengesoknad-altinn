package no.nav.syfo.consumer.rest.pdf

import no.nav.syfo.domain.soknad.Soknadsperiode
import no.nav.syfo.domain.soknad.Soknadstype
import no.nav.syfo.domain.soknad.Sykepengesoknad
import org.springframework.beans.factory.annotation.Value
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
class PDFRestController
@Inject constructor(private val restTemplate: RestTemplate,
                    @Value("\${pdfgen.url}") private val pdfgenUrl: String
) {

    fun getPDF(sykepengesoknad: Sykepengesoknad, fnr: String, navn: String): ByteArray {

        val pdfSoknad = PDFSoknad(sykepengesoknad, fnr, navn)

        val url = when (sykepengesoknad.type) {
            Soknadstype.ARBEIDSTAKERE -> "$pdfgenUrl/api/v1/genpdf/syfosoknader/${PDFTemplate.ARBEIDSTAKERE}"
            Soknadstype.BEHANDLINGSDAGER -> "$pdfgenUrl/api/v1/genpdf/syfosoknader/${PDFTemplate.BEHANDLINGSDAGER}"
            else -> throw RuntimeException("Har ikke implementert PDF-template for søknad av typen: ${sykepengesoknad.type}")
        }

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(pdfSoknad, headers)

        val result = restTemplate.exchange(url, HttpMethod.POST, entity, ByteArray::class.java)

        if (result.statusCode != OK) {
            throw RuntimeException("getPDFArbeidstakere feiler med HTTP-" + result.statusCode + " for søknad med id: " + sykepengesoknad.id)
        }

        return result.body ?: throw RuntimeException("pdfgen returnerer null - dette er en feil")
    }

    private class PDFSoknad(sykepengesoknad: Sykepengesoknad, val fnr: String, val navn: String) {
        val soknadsId = sykepengesoknad.id
        val soknadstype = sykepengesoknad.type
        val innsendtDato = sykepengesoknad.sendtNav?.toLocalDate()
        val sendtArbeidsgiver = sykepengesoknad.sendtArbeidsgiver?.toLocalDate()
        val sykmeldingUtskrevet = sykepengesoknad.sykmeldingSkrevet?.toLocalDate()
        val arbeidsgiver = sykepengesoknad.arbeidsgiver.navn
        val korrigerer = sykepengesoknad.korrigerer
        val soknadPerioder = sykepengesoknad.soknadsperioder.map { PDFPeriode(it) }
        val avsendertype = sykepengesoknad.avsendertype
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
        val faktiskGrad = soknadsperiode.faktiskGrad
        val avtaltTimer = soknadsperiode.avtaltTimer
        val faktiskTimer = soknadsperiode.faktiskTimer
        val sykmeldingstype = soknadsperiode.sykmeldingstype
    }

}
