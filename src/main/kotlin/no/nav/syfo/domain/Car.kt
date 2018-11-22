package no.nav.syfo.domain

import java.time.LocalDate

data class Car(
    val uuid: String? = null,
    val color: String? = null,
    val brand: String? = null,
    val bought: LocalDate? = null
)
