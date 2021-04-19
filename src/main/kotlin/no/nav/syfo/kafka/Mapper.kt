package no.nav.syfo.kafka

import no.nav.syfo.domain.soknad.*
import no.nav.syfo.kafka.felles.*
import no.nav.syfo.kafka.felles.SykepengesoknadDTO

fun SykepengesoknadDTO.konverter(): Sykepengesoknad {
    return Sykepengesoknad(
        id = this.id,
        type = Soknadstype.valueOf(this.type.name),
        status = Soknadsstatus.valueOf(this.status.name),
        fnr = this.fnr,
        sykmeldingId = this.sykmeldingId,
        arbeidsgiver = konverter(this.arbeidsgiver!!),
        arbeidssituasjon = enumValueOrNull(this.arbeidssituasjon!!.name),
        korrigertAv = this.korrigertAv,
        korrigerer = this.korrigerer,
        soktUtenlandsopphold = this.soktUtenlandsopphold,
        arbeidsgiverForskutterer = enumValueOrNull(this.arbeidsgiverForskutterer?.name),
        fom = this.fom,
        tom = this.tom,
        startSykeforlop = this.startSyketilfelle,
        arbeidGjenopptatt = this.arbeidGjenopptatt,
        sykmeldingSkrevet = this.sykmeldingSkrevet,
        opprettet = this.opprettet,
        sendtNav = this.sendtNav,
        sendtArbeidsgiver = this.sendtArbeidsgiver,
        behandlingsdager = this.behandlingsdager ?: emptyList(),
        egenmeldinger = this.egenmeldinger
            ?.map { konverter(it) }
            .orEmpty(),
        papirsykmeldinger = this.papirsykmeldinger
            ?.map { konverter(it) }
            .orEmpty(),
        fravar = this.fravar
            ?.map { konverter(it) }
            .orEmpty(),
        andreInntektskilder = this.andreInntektskilder
            ?.map { konverter(it) }
            .orEmpty(),
        soknadsperioder = this.soknadsperioder
            ?.map { konverter(it) }
            .orEmpty(),
        sporsmal = this.sporsmal
            ?.map { konverter(it) }
            .orEmpty(),
        ettersending = this.ettersending
    )
}

fun konverter(sykepengesoknadDTO: DeprecatedSykepengesoknadDTO, fnr: String): Sykepengesoknad {
    return Sykepengesoknad(
        id = sykepengesoknadDTO.id!!,
        type = Soknadstype.valueOf(sykepengesoknadDTO.type!!.name),
        status = Soknadsstatus.valueOf(sykepengesoknadDTO.status!!.name),
        fnr = fnr,
        sykmeldingId = sykepengesoknadDTO.sykmeldingId,
        arbeidsgiver = konverter(sykepengesoknadDTO.arbeidsgiver!!),
        arbeidssituasjon = enumValueOrNull(sykepengesoknadDTO.arbeidssituasjon!!.name),
        korrigertAv = sykepengesoknadDTO.korrigertAv,
        korrigerer = sykepengesoknadDTO.korrigerer,
        soktUtenlandsopphold = sykepengesoknadDTO.soktUtenlandsopphold,
        arbeidsgiverForskutterer = enumValueOrNull(sykepengesoknadDTO.arbeidsgiverForskutterer?.name),
        fom = sykepengesoknadDTO.fom,
        tom = sykepengesoknadDTO.tom,
        startSykeforlop = sykepengesoknadDTO.startSyketilfelle,
        arbeidGjenopptatt = sykepengesoknadDTO.arbeidGjenopptatt,
        sykmeldingSkrevet = sykepengesoknadDTO.sykmeldingSkrevet,
        opprettet = sykepengesoknadDTO.opprettet,
        sendtNav = sykepengesoknadDTO.sendtNav,
        sendtArbeidsgiver = sykepengesoknadDTO.sendtArbeidsgiver,
        behandlingsdager = sykepengesoknadDTO.behandlingsdager ?: emptyList(),
        egenmeldinger = sykepengesoknadDTO.egenmeldinger
            ?.map { konverter(it) }
            .orEmpty(),
        papirsykmeldinger = sykepengesoknadDTO.papirsykmeldinger
            ?.map { konverter(it) }
            .orEmpty(),
        fravar = sykepengesoknadDTO.fravar
            ?.map { konverter(it) }
            .orEmpty(),
        andreInntektskilder = sykepengesoknadDTO.andreInntektskilder
            ?.map { konverter(it) }
            .orEmpty(),
        soknadsperioder = sykepengesoknadDTO.soknadsperioder
            ?.map { konverter(it) }
            .orEmpty(),
        sporsmal = sykepengesoknadDTO.sporsmal
            ?.map { konverter(it) }
            .orEmpty(),
        ettersending = sykepengesoknadDTO.ettersending
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
        sykmeldingstype = enumValueOrNull(soknadPeriodeDTO.sykmeldingstype!!.name)
    )
}

private fun konverter(arbeidsgiverDTO: ArbeidsgiverDTO): Arbeidsgiver {
    return Arbeidsgiver(
        navn = arbeidsgiverDTO.navn!!,
        orgnummer = arbeidsgiverDTO.orgnummer!!
    )
}

private fun konverter(periodeDTO: PeriodeDTO): Periode {
    return Periode(
        fom = periodeDTO.fom!!,
        tom = periodeDTO.tom!!
    )
}

private fun konverter(fravarDTO: FravarDTO): Fravar {
    return Fravar(
        fom = fravarDTO.fom!!,
        tom = fravarDTO.tom,
        type = Fravarstype.valueOf(fravarDTO.type!!.name)
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
