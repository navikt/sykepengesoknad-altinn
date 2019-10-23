package no.nav.syfo.kafka

import no.nav.syfo.mockSykepengesoknadDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MapperKtTest {

    @Test
    fun mapperDTOTilSykepengesoknad() {
        val sykepengesoknad = konverter(mockSykepengesoknadDTO)

        assertThat(sykepengesoknad.id).isEqualTo("d053fef8-6f2e-4d45-bc9f-ed6c5cd457dd")
        assertThat(sykepengesoknad.ettersending).isFalse()
    }
}

