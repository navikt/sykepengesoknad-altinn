package no.nav.syfo.orgnummer

import no.nav.syfo.Testoppsett
import org.amshove.kluent.*
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.util.*

class OrgnummerTest : Testoppsett() {

    @Autowired
    private lateinit var juridiskOrgnummerRepository: JuridiskOrgnummerRepository

    @Test
    fun `Sendt arbeidstaker søknad mottas og sendes til altinn`() {
        juridiskOrgnummerRepository.deleteAll()
        juridiskOrgnummerRepository.count() `should be equal to` 0

        val orgnummer1 = "111111111"
        val juridiskOrgnummer1 = "22222222"

        leggSykmeldingPåKafka(
            skapSykmeldingKafkaMessage(
                orgnummer = orgnummer1,
                juridiskOrgnummer = juridiskOrgnummer1
            )
        )

        await().atMost(Duration.ofSeconds(10))
            .until {
                juridiskOrgnummerRepository.count() == 1L
            }

        juridiskOrgnummerRepository.findByOrgnummer(orgnummer1)!!.juridiskOrgnummer `should be equal to` juridiskOrgnummer1

        val orgnummer2 = "4444444"
        val juridiskOrgnummer2 = "3333333"

        leggSykmeldingPåKafka(
            skapSykmeldingKafkaMessage(
                orgnummer = orgnummer1,
                juridiskOrgnummer = juridiskOrgnummer2
            )
        )
        leggSykmeldingPåKafka(
            skapSykmeldingKafkaMessage(
                orgnummer = orgnummer2,
                juridiskOrgnummer = juridiskOrgnummer2
            )
        )
        await().atMost(Duration.ofSeconds(10))
            .until {
                juridiskOrgnummerRepository.count() == 2L
            }

        juridiskOrgnummerRepository.findByOrgnummer(orgnummer1)!!.juridiskOrgnummer `should be equal to` juridiskOrgnummer2
        juridiskOrgnummerRepository.findByOrgnummer(orgnummer2)!!.juridiskOrgnummer `should be equal to` juridiskOrgnummer2

        juridiskOrgnummerRepository.deleteAll()
    }
}
