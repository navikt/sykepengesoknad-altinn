package no.nav.syfo

import com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.kafka.konverter
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import org.mockito.Mockito

private val objectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerKotlinModule()
        .configure(READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)

val mockSykepengesoknadDTO: SykepengesoknadDTO =
        objectMapper.readValue(LocalApplication::class.java.getResource("/arbeidstakersoknad.json"), SykepengesoknadDTO::class.java)

val mockSykepengesoknad: Sykepengesoknad
    get() {
        val sykepengesoknad = konverter(mockSykepengesoknadDTO)
        sykepengesoknad.fnr = "12345678910"
        sykepengesoknad.navn = "Navn Navnesen"
        sykepengesoknad.juridiskOrgnummerArbeidsgiver = "999999999"
        sykepengesoknad.xml = sykepengesoknad2XMLByteArray(sykepengesoknad, mutableListOf())
        return sykepengesoknad
    }

// Hjelpemetoder for å brygge bro mellom nullbare argumenter i Mockitos argumentMatcher og Kotlin
fun <T> any(): T {
    Mockito.any<T>()
    return uninitialized()
}

@Suppress("UNCHECKED_CAST")
private fun <T> uninitialized(): T = null as T
// END
