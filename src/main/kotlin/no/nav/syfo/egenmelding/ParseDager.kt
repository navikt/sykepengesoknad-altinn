package no.nav.syfo.egenmelding

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.syfo.domain.soknad.Periode
import no.nav.syfo.objectMapper
import java.time.LocalDate

fun EgenmeldingFraSykmelding.egenmeldingsdager(): List<Periode> =
    egenmeldingssvar
        .let { objectMapper.readValue(it) as List<String> }
        .map { LocalDate.parse(it) }
        .groupConsecutiveDays()

fun List<LocalDate>.groupConsecutiveDays(): List<Periode> {
    if (this.isEmpty()) {
        return emptyList()
    }
    return this
        .sorted()
        .fold(emptyList()) { perioder, dato ->
            if (perioder.isEmpty() || perioder.last().tom.plusDays(1) != dato) {
                perioder + Periode(dato, dato)
            } else {
                perioder.dropLast(1) + Periode(fom = perioder.last().fom, tom = dato)
            }
        }
}
