package no.nav.syfo.domain.soknad

import java.time.LocalDate

data class SoknadPeriode(

        val fom: LocalDate? = null,
        val tom: LocalDate? = null,
        val grad: Int? = null
)
