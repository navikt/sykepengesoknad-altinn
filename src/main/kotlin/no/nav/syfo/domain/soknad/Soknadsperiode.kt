package no.nav.syfo.domain.soknad

import java.time.LocalDate

data class Soknadsperiode(

        val fom: LocalDate,
        val tom: LocalDate,
        val sykmeldingsgrad: Int,
        val faktiskGrad: Int? = null,
        val avtaltTimer: Double? = null,
        val faktiskTimer: Double? = null,
        val sykmeldingstype: Sykmeldingstype? = null

)