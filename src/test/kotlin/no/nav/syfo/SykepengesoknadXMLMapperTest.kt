package no.nav.syfo

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import javax.xml.bind.ValidationEvent

class SykepengesoknadXMLMapperTest {

    @Test
    fun mapperSykepengesoknadTilXML() {
        val validationEventer: MutableList<ValidationEvent> = mutableListOf()
        sykepengesoknad2XMLByteArray(mockSykepengesoknad.first, validationEventer, "fnr", "orgnr")

        assertThat(validationEventer).isEmpty()
    }

    @Test
    fun mapperBehandlingsdagerTilXML() {
        val validationEventer: MutableList<ValidationEvent> = mutableListOf()
        sykepengesoknad2XMLByteArray(mockSykepengesoknadBehandlingsdager, validationEventer, "fnr", "orgnr")

        assertThat(validationEventer).isEmpty()
    }
}
