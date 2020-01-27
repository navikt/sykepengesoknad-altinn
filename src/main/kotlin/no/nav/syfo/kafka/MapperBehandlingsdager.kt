package no.nav.syfo.kafka

import no.nav.syfo.domain.soknad.*
import no.nav.syfo.kafka.felles.*
import no.nav.syfo.kafka.sykepengesoknadbehandlingsdager.dto.SykepengesoknadBehandlingsdagerDTO

fun konverter(sykepengesoknadDTO: SykepengesoknadBehandlingsdagerDTO): Sykepengesoknad {
    return Sykepengesoknad(
            id = sykepengesoknadDTO.soknadFelles.id,
            aktorId = sykepengesoknadDTO.soknadFelles.aktorId,
            //TODO: ta i bruk fnr fra DTO
            type = Soknadstype.BEHANDLINGSDAGER,
            status = Soknadsstatus.valueOf(sykepengesoknadDTO.soknadFelles.status.name),
            sykmeldingId = sykepengesoknadDTO.sykepengesoknadFelles.sykmeldingId,
            arbeidsgiver = konverter(sykepengesoknadDTO.soknadFelles.arbeidsgiver),
            arbeidssituasjon = enumValueOrNull(sykepengesoknadDTO.soknadFelles.arbeidssituasjon?.name),
            korrigertAv = sykepengesoknadDTO.soknadFelles.korrigertAv,
            korrigerer = sykepengesoknadDTO.soknadFelles.korrigerer,
            arbeidsgiverForskutterer = enumValueOrNull(sykepengesoknadDTO.soknadFelles.arbeidsgiverForskutterer?.name),
            fom = sykepengesoknadDTO.sykepengesoknadFelles.fom,
            tom = sykepengesoknadDTO.sykepengesoknadFelles.tom,
            startSykeforlop = sykepengesoknadDTO.sykepengesoknadFelles.startSyketilfelle,
            sykmeldingSkrevet = sykepengesoknadDTO.sykepengesoknadFelles.sykmeldingSkrevet,
            opprettet = sykepengesoknadDTO.soknadFelles.opprettet,
            sendtNav = sykepengesoknadDTO.soknadFelles.sendtNav,
            sendtArbeidsgiver = sykepengesoknadDTO.soknadFelles.sendtArbeidsgiver,
            behandlingsdager = sykepengesoknadDTO.behandlingsdager,
            egenmeldinger = sykepengesoknadDTO.egenmeldinger
                    ?.map { konverter(it) }
                    .orEmpty(),
            papirsykmeldinger = sykepengesoknadDTO.papirsykmeldinger
                    ?.map { konverter(it) }
                    .orEmpty(),
            andreInntektskilder = sykepengesoknadDTO.andreInntektskilder
                    ?.map { konverter(it) }
                    .orEmpty(),
            soknadsperioder = sykepengesoknadDTO.sykepengesoknadFelles.soknadsperioder
                    .map { konverter(it) },
            sporsmal = sykepengesoknadDTO.soknadFelles.sporsmal
                    .map { konverter(it) },
            ettersending = sykepengesoknadDTO.soknadFelles.ettersending
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
            svartype = Svartype.valueOf(sporsmalDTO.svartype!!.name),
            min = sporsmalDTO.min,
            max = sporsmalDTO.max,
            kriterieForVisningAvUndersporsmal = enumValueOrNull(sporsmalDTO.kriterieForVisningAvUndersporsmal?.name),
            svar = sporsmalDTO.svar
                    ?.map { konverter(it) }
                    .orEmpty(),
            undersporsmal = sporsmalDTO.undersporsmal
                    ?.map { konverter(it) }
                    .orEmpty()
    )
}

private fun konverter(soknadPeriodeDTO: SoknadsperiodeDTO): Soknadsperiode {
    return Soknadsperiode(
            fom = soknadPeriodeDTO.fom!!,
            tom = soknadPeriodeDTO.tom!!,
            sykmeldingsgrad = soknadPeriodeDTO.sykmeldingsgrad!!,
            faktiskGrad = soknadPeriodeDTO.faktiskGrad,
            avtaltTimer = soknadPeriodeDTO.avtaltTimer,
            faktiskTimer = soknadPeriodeDTO.faktiskTimer,
            sykmeldingstype = enumValueOrNull(soknadPeriodeDTO.sykmeldingstype!!.name))
}

private fun konverter(arbeidsgiverDTO: ArbeidsgiverDTO?): Arbeidsgiver {
    return Arbeidsgiver(
            navn = arbeidsgiverDTO?.navn ?: "",
            orgnummer = arbeidsgiverDTO?.orgnummer ?: ""
    )
}

private fun konverter(periodeDTO: PeriodeDTO): Periode {
    return Periode(
            fom = periodeDTO.fom!!,
            tom = periodeDTO.tom!!
    )
}

private fun konverter(inntektskildeDTO: InntektskildeDTO): Inntektskilde {
    return Inntektskilde(
            type = Inntektskildetype.valueOf(inntektskildeDTO.type!!.name),
            sykmeldt = inntektskildeDTO.sykmeldt
    )
}

private inline fun <reified T : Enum<*>> enumValueOrNull(name: String?): T? =
        T::class.java.enumConstants.firstOrNull { it.name == name }
