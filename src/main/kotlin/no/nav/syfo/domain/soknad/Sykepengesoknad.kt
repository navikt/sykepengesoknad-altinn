package no.nav.syfo.domain.soknad

import no.nav.syfo.domain.Arbeidssituasjon
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.stream.Stream
import kotlin.streams.toList

data class Sykepengesoknad(

        val id: String,
        val aktorId: String,
        val sykmeldingId: String? = null,
        val type: Soknadstype? = null,
        val status: Soknadsstatus? = null,
        val fom: LocalDate? = null,
        val tom: LocalDate? = null,
        val startSykeforlop: LocalDate? = null,
        val arbeidGjenopptatt: LocalDate? = null,
        val opprettet: LocalDateTime? = null,
        val sendtNav: LocalDateTime? = null,
        val sendtArbeidsgiver: LocalDateTime? = null,
        val sykmeldingSkrevet: LocalDateTime? = null,
        val arbeidsgiver: Arbeidsgiver,
        val arbeidsgiverForskutterer: ArbeidsverForskutterer,
        val soktUtenlandsopphold: Boolean? = null,
        val korrigerer: String? = null,
        val korrigertAv: String? = null,
        val arbeidssituasjon: Arbeidssituasjon? = null,
        val soknadsperioder: List<Soknadsperiode> = arrayListOf(),
        val egenmeldinger: List<Periode> = arrayListOf(),
        val papirsykmeldinger: List<Periode> = arrayListOf(),
        val fravar: List<Fravar> = arrayListOf(),
        val andreInntektskilder: List<Inntektskilde> = arrayListOf(),
        val sporsmal: List<Sporsmal> = arrayListOf()

) {
    fun alleSporsmalOgUndersporsmal(): List<Sporsmal> {
        return flatten(sporsmal)
                .toList()
    }

    fun getSporsmalMedTag(tag: String): Sporsmal {
        return alleSporsmalOgUndersporsmal().stream()
                .filter { s -> s.tag.equals(tag) }
                .findFirst()
                .orElseThrow { RuntimeException("Søknaden inneholder ikke spørsmål med tag: $tag") }
    }

    private fun flatten(nonFlatList: List<Sporsmal>): Stream<Sporsmal> {
        return nonFlatList.stream()
                .flatMap { sporsmal -> Stream.concat(Stream.of(sporsmal), flatten(sporsmal.undersporsmal)) }
    }
}
