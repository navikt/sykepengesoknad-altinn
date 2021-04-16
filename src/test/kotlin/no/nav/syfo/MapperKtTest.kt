package no.nav.syfo

import no.nav.syfo.kafka.konverter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MapperKtTest {

    @Test
    fun mapperDTOTilSykepengesoknad() {
        val sykepengesoknad = konverter(mockSykepengesoknadDTO)

        assertThat(sykepengesoknad.id).isEqualTo("d053fef8-6f2e-4d45-bc9f-ed6c5cd457dd")
        assertThat(sykepengesoknad.ettersending).isFalse()
    }
}
