package no.nav.syfo.domain.pdf

import no.nav.syfo.domain.soknad.Arbeidsgiver
import no.nav.syfo.domain.soknad.Soknadsstatus
import no.nav.syfo.domain.soknad.Soknadstype
import no.nav.syfo.domain.soknad.Sporsmal
import no.nav.syfo.domain.soknad.Sykepengesoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PDFSoknadTest {

    @Test
    fun filtrererBortAndreInntektskilder() {
        val sykepengesoknad = Sykepengesoknad(
            id = "abc",
            fnr = "123",
            type = Soknadstype.ARBEIDSTAKERE,
            status = Soknadsstatus.SENDT,
            arbeidsgiver = Arbeidsgiver("abc", "123"),
            sporsmal = listOf(
                Sporsmal(
                    tag = "ANDRE_INNTEKTSKILDER"
                ),
                Sporsmal(
                    tag = "ANDRE_INNTEKTSKILDER_V2"
                )
            )
        )
        val pdf = PDFSoknad(sykepengesoknad, "123", "abc")

        assertThat(pdf.sporsmal.size).isEqualTo(0)
    }

    @Test
    fun filtrererBortArbeidUtland() {
        val sykepengesoknad = Sykepengesoknad(
            id = "abc",
            fnr = "123",
            type = Soknadstype.ARBEIDSTAKERE,
            status = Soknadsstatus.SENDT,
            arbeidsgiver = Arbeidsgiver("abc", "123"),
            sporsmal = listOf(
                Sporsmal(
                    tag = "ARBEID_UTENFOR_NORGE"
                ),
                Sporsmal(
                    tag = "ANSVARSERKLARING"
                )
            )
        )
        val pdf = PDFSoknad(sykepengesoknad, "123", "abc")

        assertThat(pdf.sporsmal.size).isEqualTo(1)
    }

    @Test
    fun `filtrerer bort utenlandsk sykmelding spm`() {
        val sykepengesoknad = Sykepengesoknad(
            id = "abc",
            fnr = "123",
            type = Soknadstype.ARBEIDSTAKERE,
            status = Soknadsstatus.SENDT,
            arbeidsgiver = Arbeidsgiver("abc", "123"),
            sporsmal = listOf(
                Sporsmal(
                    tag = "UTENLANDSK_SYKMELDING_BOSTED"
                ),
                Sporsmal(
                    tag = "UTENLANDSK_SYKMELDING_TRYGD_UTENFOR_NORGE"
                ),
                Sporsmal(
                    tag = "UTENLANDSK_SYKMELDING_LONNET_ARBEID_UTENFOR_NORGE"
                ),
                Sporsmal(
                    tag = "ANSVARSERKLARING"
                )
            )
        )
        val pdf = PDFSoknad(sykepengesoknad, "123", "abc")

        assertThat(pdf.sporsmal.size).isEqualTo(1)
    }
}
