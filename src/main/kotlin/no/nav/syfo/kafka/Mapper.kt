package no.nav.syfo.kafka

import no.nav.syfo.domain.soknad.*
import no.nav.syfo.kafka.sykepengesoknad.dto.*
import java.util.stream.Collectors

fun konverter(sykepengesoknadDTO: SykepengesoknadDTO): Sykepengesoknad {
    return Sykepengesoknad(
            id = sykepengesoknadDTO.id,
            type = Soknadstype.valueOf(sykepengesoknadDTO.type.name),
            status = Soknadsstatus.valueOf(sykepengesoknadDTO.status.name),
            aktorId = sykepengesoknadDTO.aktorId,
            sykmeldingId = sykepengesoknadDTO.sykmeldingId,
            arbeidsgiver = konverter(sykepengesoknadDTO.arbeidsgiver),
            arbeidssituasjon = enumValueOrNull(sykepengesoknadDTO.arbeidssituasjon.name),
            korrigertAv = sykepengesoknadDTO.korrigertAv,
            korrigerer = sykepengesoknadDTO.korrigerer,
            soktUtenlandsopphold = sykepengesoknadDTO.soktUtenlandsopphold,
            arbeidsgiverForskutterer = ArbeidsverForskutterer.valueOf(sykepengesoknadDTO.arbeidsgiverForskutterer.name),
            fom = sykepengesoknadDTO.fom,
            tom = sykepengesoknadDTO.tom,
            startSykeforlop = sykepengesoknadDTO.startSykeforlop,
            arbeidGjenopptatt = sykepengesoknadDTO.arbeidGjenopptatt,
            sykmeldingSkrevet = sykepengesoknadDTO.sykmeldingSkrevet,
            opprettet = sykepengesoknadDTO.opprettet,
            sendtNav = sykepengesoknadDTO.sendtNav,
            sendtArbeidsgiver = sykepengesoknadDTO.sendtArbeidsgiver,
            egenmeldinger = sykepengesoknadDTO.egenmeldinger.stream()
                    .map(::konverter)
                    .collect(Collectors.toList()),
            papirsykmeldinger = sykepengesoknadDTO.papirsykmeldinger.stream()
                    .map(::konverter)
                    .collect(Collectors.toList()),
            fravar = sykepengesoknadDTO.fravar.stream()
                    .map(::konverter)
                    .collect(Collectors.toList()),
            andreInntektskilder = sykepengesoknadDTO.andreInntektskilder.stream()
                    .map(::konverter)
                    .collect(Collectors.toList()),
            soknadsperioder = sykepengesoknadDTO.soknadPerioder.stream()
                    .map(::konverter)
                    .collect(Collectors.toList()),
            sporsmal = sykepengesoknadDTO.sporsmal.stream()
                    .map(::konverter)
                    .collect(Collectors.toList())
    )
}

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
            svartype = Svartype.valueOf(sporsmalDTO.svartype.name),
            min = sporsmalDTO.min,
            max = sporsmalDTO.max,
            kriterieForVisningAvUndersporsmal = enumValueOrNull(sporsmalDTO.kriterieForVisningAvUndersporsmal.name),
            svar = sporsmalDTO.svar.stream()
                    .map(::konverter)
                    .collect(Collectors.toList()),
            undersporsmal = sporsmalDTO.undersporsmal.stream()
                    .map(::konverter)
                    .collect(Collectors.toList())
    )
}

private fun konverter(soknadPeriodeDTO: SoknadsperiodeDTO): Soknadsperiode {
    return Soknadsperiode(
            fom = soknadPeriodeDTO.fom,
            tom = soknadPeriodeDTO.tom,
            sykmeldingsgrad = soknadPeriodeDTO.sykmeldingGrad,
            faktiskGrad = soknadPeriodeDTO.faktiskGrad,
            avtaltTimer = soknadPeriodeDTO.avtaltTimer,
            faktiskTimer = soknadPeriodeDTO.faktiskTimer,
            sykmeldingstype = enumValueOrNull(soknadPeriodeDTO.sykmeldingtype.name))
}

private fun konverter(arbeidsgiverDTO: ArbeidsgiverDTO): Arbeidsgiver {
    return Arbeidsgiver(
            navn = arbeidsgiverDTO.navn,
            orgnummer = arbeidsgiverDTO.orgnummer
    )
}

private fun konverter(periodeDTO: PeriodeDTO): Periode {
    return Periode(
            fom = periodeDTO.fom,
            tom = periodeDTO.tom
    )
}

private fun konverter(fravarDTO: FravarDTO): Fravar {
    return Fravar(
            fom = fravarDTO.fom,
            tom = fravarDTO.tom,
            type = Fravarstype.valueOf(fravarDTO.type.name)
    )
}

private fun konverter(inntektskildeDTO: InntektskildeDTO): Inntektskilde {
    return Inntektskilde(
            type = Inntektskildetype.valueOf(inntektskildeDTO.type.name),
            sykmeldt = inntektskildeDTO.sykmeldt
    )
}

private inline fun <reified T : Enum<*>> enumValueOrNull(name: String?): T? =
        T::class.java.enumConstants.firstOrNull { it.name == name }