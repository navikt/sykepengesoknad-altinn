package no.nav.syfo.domain.soknad

import java.time.LocalDate

data class Periode(
    val fom: LocalDate,
    val tom: LocalDate
)
