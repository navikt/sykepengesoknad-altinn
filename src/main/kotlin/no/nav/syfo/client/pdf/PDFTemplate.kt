package no.nav.syfo.client.pdf

enum class PDFTemplate(
    private val endpoint: String,
) {
    ARBEIDSTAKERE("arbeidstakere"),
    SELVSTENDIGNAERINGSDRIVENDE("selvstendignaeringsdrivende"),
    SYKEPENGERUTLAND("sykepengerutland"),
    GRADERTREISETILSKUDD("gradertreisetilskudd"),
    BEHANDLINGSDAGER("behandlingsdager"),
    ;

    override fun toString(): String = this.endpoint
}
