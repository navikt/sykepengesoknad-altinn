package no.nav.syfo

import no.nav.syfo.domain.AltinnInnsendelseEkstraData
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.kafka.felles.DeprecatedSykepengesoknadDTO
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import no.nav.syfo.kafka.konverter

val mockDeprecatedSykepengesoknadDTO: DeprecatedSykepengesoknadDTO =
    objectMapper.readValue(Application::class.java.getResource("/arbeidstakersoknad.json"), DeprecatedSykepengesoknadDTO::class.java)

val mockSykepengesoknadDTO: SykepengesoknadDTO =
    objectMapper.readValue(Application::class.java.getResource("/arbeidstakersoknad.json"), SykepengesoknadDTO::class.java)

val mockSykepengesoknad: Pair<Sykepengesoknad, AltinnInnsendelseEkstraData>
    get() {
        val sykepengesoknad = konverter(mockDeprecatedSykepengesoknadDTO, "sdfs")
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

fun Any.serialisertTilString(): String = objectMapper.writeValueAsString(this)
