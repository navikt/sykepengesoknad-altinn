package no.nav.syfo.consumer.rest.pdf

enum class PDFTemplate(private val endpoint: String) {
    ARBEIDSTAKERE("arbeidstakere"),
    SELVSTENDIGNAERINGSDRIVENDE("selvstendignaeringsdrivende"),
    SYKEPENGERUTLAND("sykepengerutland"),
    BEHANDLINGSDAGER("behandlingsdager");

    override fun toString(): String {
        return this.endpoint
    }
}
