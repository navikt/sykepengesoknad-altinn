package no.nav.syfo.domain

enum class Arbeidssituasjon(
    val value: String,
) {
    NAERINGSDRIVENDE("selvstendig næringsdrivende"),
    FRILANSER("frilanser"),
    ARBEIDSTAKER("arbeidstaker"),
    ;

    override fun toString() = value
}
