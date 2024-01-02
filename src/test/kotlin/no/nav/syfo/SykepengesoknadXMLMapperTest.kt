package no.nav.syfo

import no.nav.syfo.domain.soknad.Periode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import javax.xml.bind.ValidationEvent

class SykepengesoknadXMLMapperTest {
    @Test
    fun mapperSykepengesoknadTilXML() {
        val validationEventer: MutableList<ValidationEvent> = mutableListOf()
        sykepengesoknad2XMLByteArray(mockSykepengesoknad.first, validationEventer, "fnr", "orgnr", null)

        assertThat(validationEventer).isEmpty()
    }

    @Test
    fun mapperBehandlingsdagerTilXML() {
        val validationEventer: MutableList<ValidationEvent> = mutableListOf()
        sykepengesoknad2XMLByteArray(mockSykepengesoknadBehandlingsdager, validationEventer, "fnr", "orgnr", null)

        assertThat(validationEventer).isEmpty()
    }

    @Test
    fun mapperGradertReisetilskuddSoknadTilXML() {
        val validationEventer: MutableList<ValidationEvent> = mutableListOf()
        sykepengesoknad2XMLByteArray(mockGradertReisetilskuddSykepengesoknad, validationEventer, "fnr", "orgnr", null)

        assertThat(validationEventer).isEmpty()
    }

    @Test
    fun `mapper fravær før sykmeldingen`() {
        val fom = LocalDate.of(2020, 3, 15)
        val tom = LocalDate.of(2020, 3, 12)
        val soknad =
            mockSykepengesoknad.first.copy(
                fravarForSykmeldingen =
                    listOf(
                        Periode(
                            fom = fom,
                            tom = tom,
                        ),
                    ),
                egenmeldinger = emptyList(),
            )
        val xmlArbeidsgiver =
            sykepengesoknad2XMLArbeidsgiver(soknad, "fnr", "orgnr", null)

        assertThat(xmlArbeidsgiver.sykepengesoeknad.fravaer.egenmeldingsperiodeListe).hasSize(1)
        assertThat(xmlArbeidsgiver.sykepengesoeknad.fravaer.egenmeldingsperiodeListe[0].fom).isEqualTo(fom)
        assertThat(xmlArbeidsgiver.sykepengesoeknad.fravaer.egenmeldingsperiodeListe[0].tom).isEqualTo(tom)
    }

    @Test
    fun `mapper egenmelding`() {
        val fom = LocalDate.of(2020, 3, 15)
        val tom = LocalDate.of(2020, 3, 12)
        val soknad =
            mockSykepengesoknad.first.copy(
                egenmeldinger =
                    listOf(
                        Periode(
                            fom = fom,
                            tom = tom,
                        ),
                    ),
            )
        val xmlArbeidsgiver =
            sykepengesoknad2XMLArbeidsgiver(soknad, "fnr", "orgnr", null)

        assertThat(xmlArbeidsgiver.sykepengesoeknad.fravaer.egenmeldingsperiodeListe).hasSize(1)
        assertThat(xmlArbeidsgiver.sykepengesoeknad.fravaer.egenmeldingsperiodeListe[0].fom).isEqualTo(fom)
        assertThat(xmlArbeidsgiver.sykepengesoeknad.fravaer.egenmeldingsperiodeListe[0].tom).isEqualTo(tom)
    }
}
