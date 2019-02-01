package no.nav.syfo

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.kafka.konverter
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO

private val objectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)

val mockSykepengesoknadDTO: SykepengesoknadDTO =
        objectMapper.readValue(LocalApplication::class.java.getResource("/arbeidstakersoknad.json"), SykepengesoknadDTO::class.java)

val mockSykepengesoknad: Sykepengesoknad = oppretttMockSykepengesoknad()

private fun oppretttMockSykepengesoknad(): Sykepengesoknad {
    val sykepengesoknad = konverter(mockSykepengesoknadDTO)
    sykepengesoknad.fnr = "12345678910"
    sykepengesoknad.navn = "Navn Navnesen"
    sykepengesoknad.juridiskOrgnummerArbeidsgiver = "999999999"
    return sykepengesoknad
}
