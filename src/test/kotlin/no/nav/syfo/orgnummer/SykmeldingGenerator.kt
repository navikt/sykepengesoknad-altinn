package no.nav.syfo.orgnummer

import no.nav.syfo.domain.Arbeidssituasjon
import no.nav.syfo.domain.SykmeldingKafkaMessage
import no.nav.syfo.model.Merknad
import no.nav.syfo.model.sykmelding.arbeidsgiver.ArbeidsgiverAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.ArbeidsgiverSykmelding
import no.nav.syfo.model.sykmelding.arbeidsgiver.BehandlerAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.KontaktMedPasientAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.SykmeldingsperiodeAGDTO
import no.nav.syfo.model.sykmelding.model.*
import no.nav.syfo.model.sykmeldingstatus.*
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

fun skapSykmeldingKafkaMessage(
    orgnummer: String,
    juridiskOrgnummer: String? = null,
    sykmeldingId: String = UUID.randomUUID().toString(),
    ekstraSporsmal: List<SporsmalOgSvarDTO> = emptyList(),
    erSvarOppdatering: Boolean? = null,
): SykmeldingKafkaMessage {
    val fnr = "12354324"
    val basisdato = LocalDate.now()
    val sykmeldingStatusKafkaMessageDTO =
        skapSykmeldingStatusKafkaMessageDTO(
            sykmeldingId = sykmeldingId,
            fnr = fnr,
            arbeidssituasjon = Arbeidssituasjon.ARBEIDSTAKER,
            statusEvent = STATUS_SENDT,
            arbeidsgiver =
                ArbeidsgiverStatusDTO(
                    orgnummer = orgnummer,
                    orgNavn = "Kebabbiten",
                    juridiskOrgnummer = juridiskOrgnummer,
                ),
            ekstraSporsmal = ekstraSporsmal,
            erSvarOppdatering = erSvarOppdatering,
        )
    val sykmelding =
        getSykmeldingDto(
            sykmeldingId = sykmeldingId,
            fom = basisdato.minusDays(20),
            tom = basisdato.plusDays(15),
            merknader = listOf(Merknad(type = "UGYLDIG_TILBAKEDATERING", beskrivelse = "Hey")),
        ).copy(harRedusertArbeidsgiverperiode = true)

    return SykmeldingKafkaMessage(
        sykmelding = sykmelding,
        event = sykmeldingStatusKafkaMessageDTO.event,
        kafkaMetadata = sykmeldingStatusKafkaMessageDTO.kafkaMetadata,
    )
}

private fun getSykmeldingDto(
    sykmeldingId: String = UUID.randomUUID().toString(),
    fom: LocalDate = LocalDate.of(2020, 2, 1),
    tom: LocalDate = LocalDate.of(2020, 2, 15),
    type: PeriodetypeDTO = PeriodetypeDTO.AKTIVITET_IKKE_MULIG,
    reisetilskudd: Boolean = false,
    gradert: GradertDTO? = null,
    merknader: List<Merknad>? = null,
): ArbeidsgiverSykmelding =
    ArbeidsgiverSykmelding(
        id = sykmeldingId,
        sykmeldingsperioder =
            listOf(
                SykmeldingsperiodeAGDTO(
                    fom = fom,
                    tom = tom,
                    type = type,
                    reisetilskudd = reisetilskudd,
                    aktivitetIkkeMulig = null,
                    behandlingsdager = null,
                    gradert = gradert,
                    innspillTilArbeidsgiver = null,
                ),
            ),
        behandletTidspunkt = OffsetDateTime.now(ZoneOffset.UTC),
        mottattTidspunkt = OffsetDateTime.now(ZoneOffset.UTC),
        arbeidsgiver = ArbeidsgiverAGDTO(null, null),
        syketilfelleStartDato = null,
        egenmeldt = false,
        harRedusertArbeidsgiverperiode = false,
        behandler =
            BehandlerAGDTO(
                fornavn = "Lege",
                mellomnavn = null,
                etternavn = "Legesen",
                hpr = null,
                adresse =
                    AdresseDTO(
                        gate = null,
                        postnummer = null,
                        kommune = null,
                        postboks = null,
                        land = null,
                    ),
                tlf = null,
            ),
        kontaktMedPasient = KontaktMedPasientAGDTO(null),
        meldingTilArbeidsgiver = null,
        tiltakArbeidsplassen = null,
        prognose = null,
        papirsykmelding = false,
        merknader = merknader,
        utenlandskSykmelding = null,
        signaturDato = null,
    )

private fun skapSykmeldingStatusKafkaMessageDTO(
    arbeidssituasjon: Arbeidssituasjon = Arbeidssituasjon.NAERINGSDRIVENDE,
    statusEvent: String = STATUS_BEKREFTET,
    fnr: String,
    timestamp: OffsetDateTime = OffsetDateTime.now(),
    arbeidsgiver: ArbeidsgiverStatusDTO? = null,
    sykmeldingId: String = UUID.randomUUID().toString(),
    ekstraSporsmal: List<SporsmalOgSvarDTO>,
    erSvarOppdatering: Boolean?,
): SykmeldingStatusKafkaMessageDTO =
    SykmeldingStatusKafkaMessageDTO(
        event =
            SykmeldingStatusKafkaEventDTO(
                statusEvent = statusEvent,
                sykmeldingId = sykmeldingId,
                arbeidsgiver = arbeidsgiver,
                timestamp = timestamp,
                erSvarOppdatering = erSvarOppdatering,
                sporsmals =
                    arrayListOf(
                        SporsmalOgSvarDTO(
                            tekst = "Hva jobber du som?",
                            shortName = ShortNameDTO.ARBEIDSSITUASJON,
                            svartype = SvartypeDTO.ARBEIDSSITUASJON,
                            svar = arbeidssituasjon.name,
                        ),
                    ).also { it.addAll(ekstraSporsmal) },
            ),
        kafkaMetadata =
            KafkaMetadataDTO(
                sykmeldingId = sykmeldingId,
                timestamp = timestamp,
                source = "Test",
                fnr = fnr,
            ),
    )
