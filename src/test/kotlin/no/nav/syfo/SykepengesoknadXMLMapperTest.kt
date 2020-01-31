package no.nav.syfo

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import javax.xml.bind.ValidationEvent

@RunWith(MockitoJUnitRunner::class)
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
