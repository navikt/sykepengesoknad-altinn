package no.nav.syfo

import no.nav.melding.virksomhet.sykepengesoeknadarbeidsgiver.v1.sykepengesoeknadarbeidsgiver.*
import no.nav.syfo.domain.soknad.*
import no.nav.syfo.util.JAXB
import javax.xml.bind.ValidationEvent

val sykepengesoknad2XMLByteArray =
    { sykepengesoknad: Sykepengesoknad, validationeventer: MutableList<ValidationEvent>, fnr: String, juridiskOrgnummerArbeidsgiver: String? ->
        JAXB.marshallSykepengesoeknadArbeidsgiver(
            ObjectFactory().createSykepengesoeknadArbeidsgiver(
                sykepengesoknad2XMLArbeidsgiver(
                    sykepengesoknad,
                    fnr,
                    juridiskOrgnummerArbeidsgiver
                )
            )
        ) { event ->
            validationeventer.add(event)
            true
        }.toByteArray()
    }

val sykepengesoknad2XMLArbeidsgiver =
    { sykepengesoknad: Sykepengesoknad, fnr: String, juridiskOrgnummerArbeidsgiver: String? ->
        XMLSykepengesoeknadArbeidsgiver()
            .withJuridiskOrganisasjonsnummer(juridiskOrgnummerArbeidsgiver)
            .withVirksomhetsnummer(sykepengesoknad.arbeidsgiver.orgnummer)
            .withSykepengesoeknad(sykepengesoknad2XML(sykepengesoknad, fnr))
    }

private val sykepengesoknad2XML = { sykepengesoknad: Sykepengesoknad, fnr: String ->
    XMLSykepengesoeknad()
        .withSykepengesoeknadId(sykepengesoknad.id)
        .withSykmeldingId(sykepengesoknad.sykmeldingId)
        .withKorrigerer(sykepengesoknad.korrigerer)
        .withPeriode(XMLPeriode().withFom(sykepengesoknad.fom).withTom(sykepengesoknad.tom))
        .withSykmeldtesFnr(fnr)
        .withArbeidsgiverForskuttererLoenn(sykepengesoknad.arbeidsgiverForskutterer?.name ?: "IKKE_SPURT")
        .withIdentdato(sykepengesoknad.startSykeforlop)
        .withSykmeldingSkrevetDato(sykepengesoknad.sykmeldingSkrevet?.toLocalDate())
        .withArbeidGjenopptattDato(sykepengesoknad.arbeidGjenopptatt)
        .withHarBekreftetKorrektInformasjon(
            "CHECKED".equals(
                sykepengesoknad.getSporsmalMedTag("BEKREFT_OPPLYSNINGER").svar.getOrNull(
                    0
                )?.verdi
            )
        )
        .withHarBekreftetOpplysningsplikt(
            "CHECKED".equals(
                sykepengesoknad.getSporsmalMedTag("ANSVARSERKLARING").svar.getOrNull(
                    0
                )?.verdi
            )
        )
        .withFravaer(sykepengesoknad2XMLFravar(sykepengesoknad))
        .withSykmeldingsperiodeListe(soknadsperioder2XMLSykmeldingsperiode(sykepengesoknad.soknadsperioder))
        .withUtdanning(fravar2XMLUtdanning(sykepengesoknad.fravar))
        .withAnnenInntektskildeListe(emptyList())
        .withSendtTilArbeidsgiverDato(sykepengesoknad.sendtArbeidsgiver?.toLocalDate())
        .withSendtTilNAVDato(sykepengesoknad.sendtNav?.toLocalDate())
}

private val soknadsperioder2XMLSykmeldingsperiode = { soknadsperioder: List<Soknadsperiode> ->
    soknadsperioder
        .map { soknadsperiode ->
            XMLSykmeldingsperiode()
                .withGraderingsperiode(
                    XMLPeriode()
                        .withFom(soknadsperiode.fom)
                        .withTom(soknadsperiode.tom)
                )
                .withSykmeldingsgrad(soknadsperiode.sykmeldingsgrad)
                .withKorrigertArbeidstid(soknadsperiode2XMLKorrigertArbeidstid(soknadsperiode))
        }
}

private val soknadsperiode2XMLKorrigertArbeidstid = { soknadsperiode: Soknadsperiode ->
    when {
        soknadsperiode.faktiskTimer != null -> XMLKorrigertArbeidstid()
            .withArbeidstimerNormaluke(
                soknadsperiode.avtaltTimer
                    ?: throw RuntimeException("avtaltTimer er null - denne skal være satt")
            )
            .withFaktiskeArbeidstimer(
                XMLFaktiskeArbeidstimer()
                    .withArbeidstimer(soknadsperiode.faktiskTimer)
                    .withBeregnetArbeidsgrad(soknadsperiode.faktiskGrad)
            )

        soknadsperiode.faktiskGrad != null -> XMLKorrigertArbeidstid()
            .withArbeidstimerNormaluke(
                soknadsperiode.avtaltTimer
                    ?: throw RuntimeException("avtaltTimer er null - denne skal være satt")
            )
            .withArbeidsgrad(soknadsperiode.faktiskGrad)

        else -> null
    }
}

private val fravar2XMLUtdanning = { fravarListe: List<Fravar> ->
    fravarListe
        .filter { fravar -> Fravarstype.UTDANNING_DELTID == fravar.type || Fravarstype.UTDANNING_FULLTID == fravar.type }
        .map { fravar ->
            XMLUtdanning()
                .withFom(fravar.fom)
                .withErFulltidsstudie(Fravarstype.UTDANNING_FULLTID == fravar.type)
        }
        .firstOrNull()
}

private val sykepengesoknad2XMLFravar = fun(sykepengesoknad: Sykepengesoknad): XMLFravaer? {
    return if (sykepengesoknad.egenmeldinger.isNullOrEmpty() && sykepengesoknad.fravarForSykmeldingen.isNullOrEmpty() && sykepengesoknad.fravar.isNullOrEmpty()) {
        null
    } else {
        val egenmeldingerOgFravarFor = ArrayList<XMLPeriode>()
            .also {
                it.addAll(periodeListe2XMLPeriode(sykepengesoknad.egenmeldinger))
                it.addAll(periodeListe2XMLPeriode(sykepengesoknad.fravarForSykmeldingen))
            }
        XMLFravaer()
            .withFerieListe(listOrNull(fravar2XMLPeriode(sykepengesoknad.fravar, Fravarstype.FERIE)))
            .withPermisjonListe(listOrNull(fravar2XMLPeriode(sykepengesoknad.fravar, Fravarstype.PERMISJON)))
            .withOppholdUtenforNorge(
                fravar2XMLOppholdUtenforNorge(
                    sykepengesoknad.fravar,
                    sykepengesoknad.soktUtenlandsopphold
                )
            )
            .withEgenmeldingsperiodeListe(listOrNull(egenmeldingerOgFravarFor))
    }
}

private val fravar2XMLOppholdUtenforNorge = { fravar: List<Fravar>, soktUtenlandsopphold: Boolean? ->
    val periodeliste = listOrNull(fravar2XMLPeriode(fravar, Fravarstype.UTLANDSOPPHOLD))
    if (periodeliste == null) {
        null
    } else {
        XMLOppholdUtenforNorge()
            .withPeriodeListe(periodeliste)
            .withHarSoektOmSykepengerForOppholdet(soktUtenlandsopphold)
    }
}

private val periodeListe2XMLPeriode = { periodeListe: List<Periode> ->
    periodeListe
        .map { periode -> periode2XMLPeriode(periode) }
}

private val periode2XMLPeriode = { periode: Periode ->
    XMLPeriode()
        .withFom(periode.fom)
        .withTom(periode.tom)
}

private val fravar2XMLPeriode = { fravarListe: List<Fravar>, type: Fravarstype ->
    fravarListe
        .filter { fravar -> fravar.type == type }
        .map { fravar ->
            XMLPeriode()
                .withFom(fravar.fom)
                .withTom(fravar.tom)
        }
}

private inline fun <reified T> listOrNull(list: List<T>?): List<T>? {
    return if (list.isNullOrEmpty()) {
        null
    } else {
        list
    }
}
