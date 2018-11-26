package no.nav.syfo.domain.soknad

import no.nav.syfo.domain.Arbeidssituasjon
import java.time.LocalDate

data class Sykepengesoknad(

        val id: String? = null,
        val sykmeldingId: String? = null,
        val aktorId: String? = null,
        val soknadstype: Soknadstype? = null,
        val status: Soknadstatus? = null,
        val fom: LocalDate? = null,
        val tom: LocalDate? = null,
        val opprettetDato: LocalDate? = null,
        val innsendtDato: LocalDate? = null,
        val startSykeforlop: LocalDate? = null,
        val sykmeldingUtskrevet: LocalDate? = null,
        val arbeidsgiver: String? = null,
        val korrigerer: String? = null,
        val korrigertAv: String? = null,
        val arbeidssituasjon: Arbeidssituasjon? = null,
        val soknadPerioder: List<SoknadPeriode> = arrayListOf(),
        val sporsmal: List<Sporsmal> = arrayListOf()

)
