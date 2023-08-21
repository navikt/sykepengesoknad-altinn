package no.nav.syfo.domain.pdf

import no.nav.syfo.domain.soknad.*
import java.time.LocalDate

data class PDFSoknad(
    val soknadsId: String,
    val soknadstype: Soknadstype,
    val innsendtDato: LocalDate?,
    val sendtArbeidsgiver: LocalDate?,
    val sykmeldingUtskrevet: LocalDate?,
    val arbeidsgiver: String,
    val korrigerer: String?,
    val soknadPerioder: List<PDFPeriode>,
    val avsendertype: Avsendertype?,
    val sporsmal: List<Sporsmal>,
    val fnr: String,
    val navn: String,
    val egenmeldtSykmelding: Any? = null
)

data class PDFPeriode(
    val fom: LocalDate,
    val tom: LocalDate,
    val grad: Int,
    val faktiskGrad: Int?,
    val avtaltTimer: Double?,
    val faktiskTimer: Double?,
    val sykmeldingstype: Sykmeldingstype?
)

fun generatePDFSoknad(sykepengesoknad: Sykepengesoknad, fnr: String, navn: String): PDFSoknad {
    return PDFSoknad(
        soknadsId = sykepengesoknad.id,
        soknadstype = sykepengesoknad.type,
        innsendtDato = sykepengesoknad.sendtNav?.toLocalDate(),
        sendtArbeidsgiver = sykepengesoknad.sendtArbeidsgiver?.toLocalDate(),
        sykmeldingUtskrevet = sykepengesoknad.sykmeldingSkrevet?.toLocalDate(),
        arbeidsgiver = sykepengesoknad.arbeidsgiver.navn,
        korrigerer = sykepengesoknad.korrigerer,
        soknadPerioder = sykepengesoknad.soknadsperioder.map {
            PDFPeriode(
                it.fom,
                it.tom,
                it.sykmeldingsgrad,
                it.faktiskGrad,
                it.avtaltTimer,
                it.faktiskTimer,
                it.sykmeldingstype
            )
        },
        avsendertype = sykepengesoknad.avsendertype,
        sporsmal = sykepengesoknad.sporsmal.sortedWith(
            Comparator.comparingInt {
                when (it.tag) {
                    "BEKREFT_OPPLYSNINGER", "ANSVARSERKLARING" -> 1
                    "VAER_KLAR_OVER_AT" -> 2
                    else -> 0
                }
            }
        ),
        fnr = fnr,
        navn = navn
    )
}
