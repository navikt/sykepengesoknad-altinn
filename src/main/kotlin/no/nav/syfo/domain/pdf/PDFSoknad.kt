package no.nav.syfo.domain.pdf

import no.nav.syfo.domain.soknad.Soknadsperiode
import no.nav.syfo.domain.soknad.Sykepengesoknad

class PDFSoknad(sykepengesoknad: Sykepengesoknad, val fnr: String, val navn: String) {
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
        .filter{ it.tag !in listOf("ANDRE_INNTEKTSKILDER", "ARBEID_UTENFOR_NORGE") }
        .sortedWith(Comparator.comparingInt {
            when (it.tag) {
                "BEKREFT_OPPLYSNINGER", "ANSVARSERKLARING" -> 1
                "VAER_KLAR_OVER_AT" -> 2
                else -> 0
            }
        })
    val egenmeldtSykmelding = null
}

class PDFPeriode(soknadsperiode: Soknadsperiode) {
    val fom = soknadsperiode.fom
    val tom = soknadsperiode.tom
    val grad = soknadsperiode.sykmeldingsgrad
    val faktiskGrad = soknadsperiode.faktiskGrad
    val avtaltTimer = soknadsperiode.avtaltTimer
    val faktiskTimer = soknadsperiode.faktiskTimer
    val sykmeldingstype = soknadsperiode.sykmeldingstype
}
