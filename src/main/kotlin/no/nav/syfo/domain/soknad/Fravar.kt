package no.nav.syfo.domain.soknad

import java.time.LocalDate

data class Fravar(
    val fom: LocalDate,
    val tom: LocalDate?,
    val type: Fravarstype,
)
