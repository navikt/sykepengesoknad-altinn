package no.nav.syfo.domain.soknad

data class Inntektskilde(
        val type: Inntektskildetype,
        val sykmeldt: Boolean?

)
