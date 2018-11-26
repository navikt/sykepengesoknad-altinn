package no.nav.syfo.kafka

import no.nav.syfo.domain.soknad.SoknadPeriode
import no.nav.syfo.domain.soknad.Sporsmal
import no.nav.syfo.domain.soknad.Svar
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.kafka.sykepengesoknad.dto.SoknadPeriodeDTO
import no.nav.syfo.kafka.sykepengesoknad.dto.SporsmalDTO
import no.nav.syfo.kafka.sykepengesoknad.dto.SvarDTO
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import java.util.stream.Collectors

private inline fun <reified T : Enum<*>> enumValueOrNull(name: String?): T? =
        T::class.java.enumConstants.firstOrNull { it.name == name }

private fun konverter(svarDTO: SvarDTO): Svar {
    return Svar(
            verdi = svarDTO.verdi
    )
}

private fun konverter(sporsmalDTO: SporsmalDTO): Sporsmal {
    return Sporsmal(
            id = sporsmalDTO.id,
            tag = sporsmalDTO.tag,
            sporsmalstekst = sporsmalDTO.sporsmalstekst,
            undertekst = sporsmalDTO.undertekst,
            svartype = enumValueOrNull(sporsmalDTO.svartype),
            min = sporsmalDTO.min,
            max = sporsmalDTO.max,
            kriterieForVisningAvUndersporsmal = enumValueOrNull(sporsmalDTO.kriterieForVisningAvUndersporsmal),
            svar = sporsmalDTO.svar.stream()
                    .map(::konverter)
                    .collect(Collectors.toList()),
            undersporsmal = sporsmalDTO.undersporsmal.stream()
                    .map(::konverter)
                    .collect(Collectors.toList())
    )
}

private fun konverter(soknadPeriodeDTO: SoknadPeriodeDTO): SoknadPeriode {
    return SoknadPeriode(
            fom = soknadPeriodeDTO.fom,
            tom = soknadPeriodeDTO.tom,
            grad = soknadPeriodeDTO.grad
    )
}

fun konverter(sykepengesoknadDTO: SykepengesoknadDTO): Sykepengesoknad {
    return Sykepengesoknad(
            id = sykepengesoknadDTO.id,
            aktorId = sykepengesoknadDTO.aktorId,
            sykmeldingId = sykepengesoknadDTO.sykmeldingId,
            soknadstype = enumValueOrNull(sykepengesoknadDTO.soknadstype),
            status = enumValueOrNull(sykepengesoknadDTO.status),
            fom = sykepengesoknadDTO.fom,
            tom = sykepengesoknadDTO.tom,
            opprettetDato = sykepengesoknadDTO.opprettetDato,
            innsendtDato = sykepengesoknadDTO.innsendtDato,
            arbeidsgiver = sykepengesoknadDTO.arbeidsgiver,
            arbeidssituasjon = enumValueOrNull(sykepengesoknadDTO.arbeidssituasjon),
            startSykeforlop = sykepengesoknadDTO.startSykeforlop,
            sykmeldingUtskrevet = sykepengesoknadDTO.sykmeldingUtskrevet,
            korrigertAv = sykepengesoknadDTO.korrigertAv,
            korrigerer = sykepengesoknadDTO.korrigerer,
            soknadPerioder = sykepengesoknadDTO.soknadPerioder.stream()
                    .map(::konverter)
                    .collect(Collectors.toList()),
            sporsmal = sykepengesoknadDTO.sporsmal.stream()
                    .map(::konverter)
                    .collect(Collectors.toList())
    )
}