package no.nav.syfo.domain.pdf

import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.domain.soknad.Sporsmal
import no.nav.syfo.domain.soknad.Soknadstype
import no.nav.syfo.domain.soknad.Soknadsstatus
import no.nav.syfo.domain.soknad.Arbeidsgiver
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PDFSoknadTest {

    @Test
    fun filtrererBortAndreInntektskilder() {
        val sykepengesoknad = Sykepengesoknad(
            id = "abc",
            aktorId = "123",
            type = Soknadstype.ARBEIDSTAKERE,
            status = Soknadsstatus.SENDT,
            arbeidsgiver = Arbeidsgiver("abc", "123"),
            sporsmal = listOf(Sporsmal(
                tag = "ANDRE_INNTEKTSKILDER"
            ))
        )
        val pdf = PDFSoknad(sykepengesoknad, "123", "abc")

        assertThat(pdf.sporsmal.size).isEqualTo(0)
    }

    @Test
    fun filtrererBortArbeidUtland() {
        val sykepengesoknad = Sykepengesoknad(
            id = "abc",
            aktorId = "123",
            type = Soknadstype.ARBEIDSTAKERE,
            status = Soknadsstatus.SENDT,
            arbeidsgiver = Arbeidsgiver("abc", "123"),
            sporsmal = listOf(Sporsmal(
                tag = "ARBEID_UTENFOR_NORGE"
            ), Sporsmal(
                tag = "ANSVARSERKLARING"
            ))
        )
        val pdf = PDFSoknad(sykepengesoknad, "123", "abc")

        assertThat(pdf.sporsmal.size).isEqualTo(1)
    }
}
