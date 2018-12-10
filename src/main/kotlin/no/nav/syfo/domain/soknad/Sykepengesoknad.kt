package no.nav.syfo.domain.soknad

import no.nav.syfo.domain.Arbeidssituasjon
import java.time.LocalDate

data class Sykepengesoknad(

        val id: String,
        val sykmeldingId: String? = null,
        val aktorId: String,
        val soknadstype: Soknadstype? = null,
        val status: Soknadstatus? = null,
        val fom: LocalDate? = null,
        val tom: LocalDate? = null,
        val opprettetDato: LocalDate? = null,
        val innsendtDato: LocalDate? = null,
        val startSykeforlop: LocalDate? = null,
        val sykmeldingUtskrevet: LocalDate? = null,

        //TODO ekstra felter - navn og fnr trengs også for pdf
        //andre felter også - fraværsperioder osv
        var orgnummerArbeidsgiver: String = "910067494",
        var juridiskOrgnummerArbeidsgiver: String? = null,
        var arbeidsgiverForskutterer: String = "VET_IKKE",
        var fnr: String = "11111100000",
        var navn: String = "Navn Navnesen",

        val arbeidsgiver: String? = null,
        val korrigerer: String? = null,
        val korrigertAv: String? = null,
        val arbeidssituasjon: Arbeidssituasjon? = null,
        val soknadPerioder: List<SoknadPeriode> = arrayListOf(),
        val sporsmal: List<Sporsmal> = arrayListOf()

)
