package no.nav.syfo

import no.nav.melding.virksomhet.sykepengesoeknadarbeidsgiver.v1.sykepengesoeknadarbeidsgiver.*
import no.nav.syfo.domain.soknad.*
import kotlin.streams.toList

val sykepengesoeknadArbeidsgiver2XML = { sykepengesoknad: Sykepengesoknad ->
    XMLSykepengesoeknadArbeidsgiver()
            .withJuridiskOrganisasjonsnummer(sykepengesoknad.juridiskOrgnummerArbeidsgiver)
            .withVirksomhetsnummer(sykepengesoknad.arbeidsgiver.orgnummer)
            .withSykepengesoeknad(sykepengesoeknad2XML(sykepengesoknad, sykepengesoknad.fnr))
}

private val sykepengesoeknad2XML = { sykepengesoknad: Sykepengesoknad,
                                     fnr: String ->
    XMLSykepengesoeknad()
            .withSykepengesoeknadId(sykepengesoknad.id)
            .withSykmeldingId(sykepengesoknad.sykmeldingId)
            .withKorrigerer(sykepengesoknad.korrigerer)
            .withPeriode(XMLPeriode().withFom(sykepengesoknad.fom).withTom(sykepengesoknad.tom))
            .withSykmeldtesFnr(fnr)
            .withArbeidsgiverForskuttererLoenn(sykepengesoknad.arbeidsgiverForskutterer.name)
            .withIdentdato(sykepengesoknad.startSykeforlop)
            .withSykmeldingSkrevetDato(sykepengesoknad.sykmeldingSkrevet?.toLocalDate())
            .withArbeidGjenopptattDato(sykepengesoknad.arbeidGjenopptatt)
            .withHarBekreftetKorrektInformasjon("CHECKED".equals(sykepengesoknad.getSporsmalMedTag("BEKREFT_OPPLYSNINGER").svar[0]))
            .withHarBekreftetOpplysningsplikt("CHECKED".equals(sykepengesoknad.getSporsmalMedTag("ANSVARSERKLARING").svar[0]))
            .withFravaer(sykepengesoknad2XMLFravar(sykepengesoknad))
            .withSykmeldingsperiodeListe(soknadsperioder2XMLSykmeldingsperiode(sykepengesoknad.soknadsperioder))
            .withUtdanning(fravar2XMLUtdanning(sykepengesoknad.fravar))
            .withAnnenInntektskildeListe(listOrNull(andreInntektskilder2XMLAnnenInntektskildeListe(sykepengesoknad.andreInntektskilder)))
            .withSendtTilArbeidsgiverDato(sykepengesoknad.sendtArbeidsgiver?.toLocalDate())
            .withSendtTilNAVDato(sykepengesoknad.sendtNav?.toLocalDate())
}

private val soknadsperioder2XMLSykmeldingsperiode = { soknadsperioder: List<Soknadsperiode> ->
    soknadsperioder.stream()
            .map { soknadsperiode ->
                XMLSykmeldingsperiode()
                        .withGraderingsperiode(XMLPeriode()
                                .withFom(soknadsperiode.fom)
                                .withTom(soknadsperiode.tom))
                        .withSykmeldingsgrad(soknadsperiode.sykmeldingsgrad)
                        .withKorrigertArbeidstid(soknadsperiode2XMLKorrigertArbeidstid(soknadsperiode))
            }
            .toList()
}

private val soknadsperiode2XMLKorrigertArbeidstid = { soknadsperiode: Soknadsperiode ->
    when {
        soknadsperiode.faktiskTimer != null -> XMLKorrigertArbeidstid()
                .withArbeidstimerNormaluke(soknadsperiode.avtaltTimer!!)
                .withFaktiskeArbeidstimer(XMLFaktiskeArbeidstimer()
                        .withArbeidstimer(soknadsperiode.faktiskTimer)
                        .withBeregnetArbeidsgrad(soknadsperiode.faktiskGrad))

        soknadsperiode.faktiskGrad != null -> XMLKorrigertArbeidstid()
                .withArbeidstimerNormaluke(soknadsperiode.avtaltTimer!!)
                .withArbeidsgrad(soknadsperiode.faktiskGrad)

        else -> null
    }
}

private val andreInntektskilder2XMLAnnenInntektskildeListe = { andreInntektskilder: List<Inntektskilde> ->
    andreInntektskilder.stream()
            .map { inntektskilde ->
                XMLAnnenInntektskilde()
                        .withErSykmeldt(inntektskilde.sykmeldt)
                        .withType(XMLAnnenInntektskildeType.valueOf(inntektskilde.type.name))
            }
            .toList()
}

private val fravar2XMLUtdanning = { fravarListe: List<Fravar> ->
    fravarListe.stream()
            .filter { fravar -> Fravarstype.UTDANNING_DELTID == fravar.type || Fravarstype.UTDANNING_FULLTID == fravar.type }
            .map { fravar ->
                XMLUtdanning()
                        .withFom(fravar.fom)
                        .withErFulltidsstudie(Fravarstype.UTDANNING_FULLTID == fravar.type)
            }
            .findFirst()
            .orElse(null)
}

private val sykepengesoknad2XMLFravar = { sykepengesoknad: Sykepengesoknad ->
    if (sykepengesoknad.egenmeldinger.isNullOrEmpty() && sykepengesoknad.fravar.isNullOrEmpty())
        null
    else
        XMLFravaer()
                .withFerieListe(listOrNull(fravar2XMLPeriode(sykepengesoknad.fravar, Fravarstype.FERIE)))
                .withPermisjonListe(listOrNull(fravar2XMLPeriode(sykepengesoknad.fravar, Fravarstype.PERMISJON)))
                .withOppholdUtenforNorge(fravar2XMLOppholdUtenforNorge(sykepengesoknad.fravar, sykepengesoknad.soktUtenlandsopphold))
                .withEgenmeldingsperiodeListe(listOrNull(egenmeldinger2XMLPeriode(sykepengesoknad.egenmeldinger)))
}

private val fravar2XMLOppholdUtenforNorge = { fravar: List<Fravar>, soktUtenlandsopphold: Boolean? ->
    XMLOppholdUtenforNorge()
            .withPeriodeListe(listOrNull(fravar2XMLPeriode(fravar, Fravarstype.UTLANDSOPPHOLD)))
            .withHarSoektOmSykepengerForOppholdet(soktUtenlandsopphold)
}

private val egenmeldinger2XMLPeriode = { egenmeldinger: List<Periode> ->
    egenmeldinger.stream()
            .map { periode -> periode2XMLPeriode(periode) }
            .toList()
}

private val periode2XMLPeriode = { periode: Periode ->
    XMLPeriode()
            .withFom(periode.fom)
            .withTom(periode.tom)
}

private val fravar2XMLPeriode = { fravarListe: List<Fravar>, type: Fravarstype ->
    fravarListe.stream()
            .filter { fravar -> fravar.type == type }
            .map { fravar ->
                XMLPeriode()
                        .withFom(fravar.fom)
                        .withTom(fravar.tom)
            }
            .toList()
}

private inline fun <reified T> listOrNull(list: List<T>?): List<T>? {
    return if (list.isNullOrEmpty())
        null
    else
        list
}


