package no.nav.syfo

import no.nav.helse.flex.sykepengesoknad.kafka.*
import no.nav.syfo.domain.AltinnInnsendelseEkstraData
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.kafka.konverter

val mockSykepengesoknadDTO: SykepengesoknadDTO =
    objectMapper.readValue(
        Application::class.java.getResource("/arbeidstakersoknad.json"),
        SykepengesoknadDTO::class.java,
    )

val mockSykepengesoknad: Pair<Sykepengesoknad, AltinnInnsendelseEkstraData>
    get() {
        val sykepengesoknad = mockSykepengesoknadDTO.konverter()
        val fnr = "12345678910"
        val navn = "Navn Navnesen"
        val juridiskOrgnummerArbeidsgiver = "999999999"
        val ekstra =
            AltinnInnsendelseEkstraData(
                fnr = fnr,
                navn = navn,
                xml = sykepengesoknad2XMLByteArray(sykepengesoknad, mutableListOf(), fnr, juridiskOrgnummerArbeidsgiver, null),
                pdf = ByteArray(0),
            )
        return Pair(sykepengesoknad, ekstra)
    }

val mockGradertReisetilskuddSykepengesoknadDTO: SykepengesoknadDTO =
    objectMapper.readValue(
        Application::class.java.getResource("/gradertreisetilskuddarbeidstakersoknad.json"),
        SykepengesoknadDTO::class.java,
    )

val mockGradertReisetilskuddSykepengesoknad: Sykepengesoknad
    get() {
        return mockGradertReisetilskuddSykepengesoknadDTO.konverter()
    }

val mockSykepengesoknadBehandlingsdagerDTO: SykepengesoknadDTO =
    objectMapper.readValue(
        Application::class.java.getResource("/behandlingsdagersoknad.json"),
        SykepengesoknadDTO::class.java,
    )

val mockSykepengesoknadBehandlingsdager: Sykepengesoknad
    get() {
        return mockSykepengesoknadBehandlingsdagerDTO.konverter()
    }
