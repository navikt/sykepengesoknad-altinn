package no.nav.syfo

import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import no.nav.syfo.orgnummer.JuridiskOrgnummer
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should be true`
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

class SendTilAltinnServiceTest : Testoppsett() {
    val grunnSoknad: SykepengesoknadDTO =
        objectMapper.readValue(
            Application::class.java.getResource("/arbeidstakersoknad.json"),
            SykepengesoknadDTO::class.java,
        )

    @Test
    fun senderIkkeTilAltinnHvisSoknadAlleredeErSendt() {
        val soknad =
            grunnSoknad.copy(
                id = UUID.randomUUID().toString(),
            )
        juridiskOrgnummerRepository.save(
            JuridiskOrgnummer(orgnummer = "12345678", juridiskOrgnummer = "LEGAL123", sykmeldingId = soknad.sykmeldingId!!),
        )

        mockPdlResponse()
        mockAltinnResponse()

        leggSøknadPåKafka(soknad)
        await().atMost(Duration.ofSeconds(10)).until {
            sendtSoknadRepository.existsBySykepengesoknadId(soknad.id)
        }

        sendtSoknadRepository.existsBySykepengesoknadId(soknad.id).`should be true`()

        pdlMockWebserver.takeRequest()
        altinnMockWebserver.takeRequest()

        leggSøknadPåKafka(soknad)

        // Sendes ikke igjen
        altinnMockWebserver.takeRequest(5, TimeUnit.SECONDS).`should be null`()
    }

    @Test
    fun ettersendingTilNavBehandlesIkke() {
        val soknad =
            grunnSoknad.copy(
                sendtArbeidsgiver = LocalDateTime.now().minusDays(1),
                sendtNav = LocalDateTime.now(),
                ettersending = true,
                id = UUID.randomUUID().toString(),
            )

        leggSøknadPåKafka(soknad)

        altinnMockWebserver.takeRequest(5, TimeUnit.SECONDS).`should be null`()
    }
}
