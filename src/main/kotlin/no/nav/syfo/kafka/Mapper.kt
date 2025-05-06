package no.nav.syfo.kafka

import no.nav.helse.flex.sykepengesoknad.kafka.*
import no.nav.syfo.domain.soknad.*

fun SykepengesoknadDTO.konverter(): Sykepengesoknad =
    Sykepengesoknad(
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
        egenmeldinger =
            this.egenmeldinger
                ?.map { konverter(it) }
                .orEmpty(),
        fravarForSykmeldingen =
            this.fravarForSykmeldingen
                ?.map { konverter(it) }
                .orEmpty(),
        papirsykmeldinger =
            this.papirsykmeldinger
                ?.map { konverter(it) }
                .orEmpty(),
        fravar =
            this.fravar
                ?.map { konverter(it) }
                .orEmpty(),
        andreInntektskilder =
            this.andreInntektskilder
                ?.map { konverter(it) }
                .orEmpty(),
        soknadsperioder =
            this.soknadsperioder
                ?.map { konverter(it) }
                .orEmpty(),
        sporsmal =
            this.sporsmal
                ?.map { konverter(it) }
                .orEmpty(),
        ettersending = this.ettersending,
    )

private fun konverter(svarDTO: SvarDTO): Svar =
    Svar(
        verdi = svarDTO.verdi,
    )

private fun konverter(sporsmalDTO: SporsmalDTO): Sporsmal =
    Sporsmal(
        id = sporsmalDTO.id,
        tag = sporsmalDTO.tag,
        sporsmalstekst = sporsmalDTO.sporsmalstekst,
        undertekst = sporsmalDTO.undertekst,
        svartype = Svartype.valueOf(sporsmalDTO.svartype!!.name),
        min = sporsmalDTO.min,
        max = sporsmalDTO.max,
        kriterieForVisningAvUndersporsmal = enumValueOrNull(sporsmalDTO.kriterieForVisningAvUndersporsmal?.name),
        svar =
            sporsmalDTO.svar
                ?.map { konverter(it) }
                .orEmpty(),
        undersporsmal =
            sporsmalDTO.undersporsmal
                ?.map { konverter(it) }
                .orEmpty(),
    )

private fun konverter(soknadPeriodeDTO: SoknadsperiodeDTO): Soknadsperiode =
    Soknadsperiode(
        fom = soknadPeriodeDTO.fom!!,
        tom = soknadPeriodeDTO.tom!!,
        sykmeldingsgrad = soknadPeriodeDTO.sykmeldingsgrad!!,
        faktiskGrad = soknadPeriodeDTO.faktiskGrad,
        avtaltTimer = soknadPeriodeDTO.avtaltTimer,
        faktiskTimer = soknadPeriodeDTO.faktiskTimer,
        sykmeldingstype = enumValueOrNull(soknadPeriodeDTO.sykmeldingstype!!.name),
    )

private fun konverter(arbeidsgiverDTO: ArbeidsgiverDTO): Arbeidsgiver =
    Arbeidsgiver(
        navn = arbeidsgiverDTO.navn!!,
        orgnummer = arbeidsgiverDTO.orgnummer!!,
    )

private fun konverter(periodeDTO: PeriodeDTO): Periode =
    Periode(
        fom = periodeDTO.fom!!,
        tom = periodeDTO.tom!!,
    )

private fun konverter(fravarDTO: FravarDTO): Fravar =
    Fravar(
        fom = fravarDTO.fom!!,
        tom = fravarDTO.tom,
        type = Fravarstype.valueOf(fravarDTO.type!!.name),
    )

private fun konverter(inntektskildeDTO: InntektskildeDTO): Inntektskilde =
    Inntektskilde(
        type = Inntektskildetype.valueOf(inntektskildeDTO.type!!.name),
        sykmeldt = inntektskildeDTO.sykmeldt,
    )

private inline fun <reified T : Enum<*>> enumValueOrNull(name: String?): T? = T::class.java.enumConstants.firstOrNull { it.name == name }
