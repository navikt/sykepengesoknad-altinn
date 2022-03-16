package no.nav.syfo.orgnummer

import no.nav.syfo.Testoppsett
import org.amshove.kluent.*
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.*

class OrgnummerTest : Testoppsett() {

    @Test
    fun `Sendt arbeidstaker sykmelding mottas og orgnumemr lagres`() {
        juridiskOrgnummerRepository.deleteAll()
        juridiskOrgnummerRepository.count() `should be equal to` 0

        val orgnummer1 = "111111111"
        val juridiskOrgnummer1 = "22222222"

        val sykmeldingKafkaMessage = skapSykmeldingKafkaMessage(
            orgnummer = orgnummer1,
            juridiskOrgnummer = juridiskOrgnummer1
        )
        leggSykmeldingPåKafka(
            sykmeldingKafkaMessage
        )

        await().atMost(Duration.ofSeconds(10))
            .until {
                juridiskOrgnummerRepository.count() == 1L
            }

        juridiskOrgnummerRepository.findBySykmeldingId(sykmeldingKafkaMessage.sykmelding.id)!!.juridiskOrgnummer `should be equal to` juridiskOrgnummer1
        juridiskOrgnummerRepository.findBySykmeldingId(sykmeldingKafkaMessage.sykmelding.id)!!.orgnummer `should be equal to` orgnummer1

        juridiskOrgnummerRepository.deleteAll()
    }
}
