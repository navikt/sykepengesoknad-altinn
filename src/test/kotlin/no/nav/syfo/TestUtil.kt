package no.nav.syfo

import com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.syfo.domain.AltinnInnsendelseEkstraData
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.kafka.felles.DeprecatedSykepengesoknadDTO
import no.nav.syfo.kafka.konverter
import org.mockito.Mockito

private val objectMapper = ObjectMapper()
    .registerModule(JavaTimeModule())
    .registerKotlinModule()
    .configure(READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)

val mockSykepengesoknadDTO: DeprecatedSykepengesoknadDTO =
    objectMapper.readValue(Application::class.java.getResource("/arbeidstakersoknad.json"), DeprecatedSykepengesoknadDTO::class.java)

val mockSykepengesoknad: Pair<Sykepengesoknad, AltinnInnsendelseEkstraData>
    get() {
        val sykepengesoknad = konverter(mockSykepengesoknadDTO, "sdfs")
        val fnr = "12345678910"
        val navn = "Navn Navnesen"
        val juridiskOrgnummerArbeidsgiver = "999999999"
        val ekstra = AltinnInnsendelseEkstraData(
            fnr = fnr,
            navn = navn,
            xml = sykepengesoknad2XMLByteArray(sykepengesoknad, mutableListOf(), fnr, juridiskOrgnummerArbeidsgiver),
            pdf = ByteArray(0)
        )
        return Pair(sykepengesoknad, ekstra)
    }

val mockSykepengesoknadBehandlingsdagerDTO: DeprecatedSykepengesoknadDTO =
    objectMapper.readValue(Application::class.java.getResource("/behandlingsdagersoknad.json"), DeprecatedSykepengesoknadDTO::class.java)

val mockSykepengesoknadBehandlingsdager: Sykepengesoknad
    get() {
        val sykepengesoknad = konverter(mockSykepengesoknadBehandlingsdagerDTO, "sdfsd")
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
