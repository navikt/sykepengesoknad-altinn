package no.nav.syfo

import no.nav.melding.virksomhet.sykepengesoeknadarbeidsgiver.v1.sykepengesoeknadarbeidsgiver.XMLSykepengesoeknad
import no.nav.melding.virksomhet.sykepengesoeknadarbeidsgiver.v1.sykepengesoeknadarbeidsgiver.XMLSykepengesoeknadArbeidsgiver
import no.nav.syfo.domain.soknad.Sykepengesoknad


//TODO gjøre denne mappingen når søknaden kommer på nytt format
    val sykepengesoeknadArbeidsgiver2XML = { sykepengesoknad: Sykepengesoknad ->
        XMLSykepengesoeknadArbeidsgiver()
                .withJuridiskOrganisasjonsnummer(sykepengesoknad.juridiskOrgnummerArbeidsgiver)
                .withVirksomhetsnummer(sykepengesoknad.orgnummerArbeidsgiver)
                .withSykepengesoeknad(XMLSykepengesoeknad())
        //.withSykepengesoeknad(sykepengesoeknad2XML(sykepengesoknad))
    }

    /*private val sykepengesoeknad2XML = { sykepengesoknad: Sykepengesoknad ->
        XMLSykepengesoeknad()
                .withSykepengesoeknadId(sykepengesoknad.id)
                .withSykmeldingId(sykepengesoknad.sykmeldingId)
                .withKorrigerer(sykepengesoknad.korrigerer)
                .withPeriode(XMLPeriode().withFom(sykepengesoknad.fom).withTom(sykepengesoknad.tom))
                .withSykmeldtesFnr(sykepengesoknad.fnr)
                .withArbeidsgiverForskuttererLoenn(sykepengesoknad.arbeidsgiverForskutterer)
                .withIdentdato(sykepengesoknad.startSykeforlop)
                .withSykmeldingSkrevetDato(sykepengesoknad.sykmeldingUtskrevet)
                .withArbeidGjenopptattDato(sykepengesoknad.arbeidGjenopptattDato)
                .withHarBekreftetKorrektInformasjon(sykepengesoknad.bekreftetKorrektInformasjon)
                .withHarBekreftetOpplysningsplikt(sykepengesoknad.bekreftetOpplysningsplikt)
                .withFravaer(mapNullable(sykepengesoknad.fravaer, fravaersperioder2XMLFravaer))
                .withSykmeldingsperiodeListe(map(sykepengesoknad, periode2XMLAktivitet))
                .withUtdanning(mapNullable(sykepengesoknad.utdanning, sykepengesoeknad2XMLUtdanning))
                .withAnnenInntektskildeListe(mapListe(sykepengesoknad.andreInntektskilder, annenInntektskilde2XML))
                .withSendtTilArbeidsgiverDato(sykepengesoknad.sendtTilArbeidsgiverDato)
                .withSendtTilNAVDato(sykepengesoknad.sendtTilNAVDato)
    }



private val fravaersperioder2XMLFravaer = { fravaersperioder ->
    XMLFravaer()
            .withEgenmeldingsperiodeListe(mapNullable(filterListe(fravaersperioder, fravaersperiodefilter(EGENMELDING)), fravaersperiode2XMLPeriode))
            .withFerieListe(mapNullable(filterListe(fravaersperioder, fravaersperiodefilter(FERIE)), fravaersperiode2XMLPeriode))
            .withPermisjonListe(mapNullable(filterListe(fravaersperioder, fravaersperiodefilter(PERMISJON)), fravaersperiode2XMLPeriode))
            .withOppholdUtenforNorge(tilOppholdUtenforNorge(fravaersperioder))
}

private val periode2XMLAktivitet = { sykepengesoeknad ->
    val virtuellePeriode = finnUtsnittAvPerioder(sykepengesoeknad.getSykmeldingDokument().perioder, sykepengesoeknad.fom, sykepengesoeknad.tom)

    virtuellePeriode.stream().map(
            { p ->
                XMLSykmeldingsperiode()
                        .withGraderingsperiode(map(p, periode2XML))
                        .withSykmeldingsgrad(p.grad)
                        .withKorrigertArbeidstid(
                                mapNullable(
                                        sykepengesoeknad.korrigertArbeidstid
                                                .stream()
                                                .filter({ ka -> ka.periode.id.equals(p.id) })
                                                .findFirst()
                                                .orElse(null),
                                        korrigertArbeidstid2XML))
            }).collect(toList())
}

private val periode2XML = { periode ->
    XMLPeriode()
            .withFom(periode.fom)
            .withTom(periode.tom)
}

private val sykepengesoeknad2XMLUtdanning = { utdanning ->
    XMLUtdanning()
            .withFom(utdanning.fom)
            .withErFulltidsstudie(utdanning.fulltidsstudie)
}

private val annenInntektskilde2XML = { annenInntektskilde ->
    XMLAnnenInntektskilde()
            .withErSykmeldt(annenInntektskilde.sykmeldt)
            .withType(map(annenInntektskilde.type, ???({ valueOf() })))
}

private val fravaersperiode2XMLPeriode = { fp ->
    fp.stream()
            .map({ fravaer -> fravaer.fravaersperioder })
            .flatMap(???({ it.stream() }))
    .map { fravaersperiode -> XMLPeriode().withFom(fravaersperiode.fom).withTom(fravaersperiode.tom) }
        .collect(toList())
}

private val korrigertArbeidstid2XML = { korrigertArbeidstid ->
    XMLKorrigertArbeidstid()
            .withArbeidstimerNormaluke(korrigertArbeidstid.arbeidstimerNormalUke)
            .withArbeidsgrad(korrigertArbeidstid.arbeidsgrad)
            .withFaktiskeArbeidstimer(mapNullable(korrigertArbeidstid.faktiskeArbeidstimer) { faktiskeArbeidstimer ->
                XMLFaktiskeArbeidstimer()
                        .withArbeidstimer(faktiskeArbeidstimer.toDouble())
                        .withBeregnetArbeidsgrad(korrigertArbeidstid.beregnetArbeidsgrad)
            })
}

private fun tilOppholdUtenforNorge(fravaersperioder: List<Fravaer>): XMLOppholdUtenforNorge? {
    return if (filterListe(fravaersperioder, fravaersperiodefilter(OPPHOLD_UTENFOR_NORGE))
                    .isEmpty())
        null
    else
        mapNullable(fravaersperioder, fravaersperiode2XMLOppholdUtenforNorge)
}

val fravaersperiode2XMLOppholdUtenforNorge = { fravaerListe ->
    XMLOppholdUtenforNorge()
            .withPeriodeListe(mapNullable(filterListe(fravaerListe, fravaersperiodefilter(OPPHOLD_UTENFOR_NORGE)), fravaersperiode2XMLPeriode))
            .withHarSoektOmSykepengerForOppholdet(fravaerListe.stream()
                    .filter(fravaersperiodefilter(OPPHOLD_UTENFOR_NORGE))
                    .findAny()
                    .map({ f -> f.soektOmSykepengerIPerioden })
                    .orElse(null))
}

private fun fravaersperiodefilter(vararg type: Fravaersperiodetype): Predicate<Fravaer> {
    return { fravaersperiode -> Stream.of(type).anyMatch({ fravaersperiodetype -> fravaersperiodetype.equals(fravaersperiode.type) }) }
}*/
