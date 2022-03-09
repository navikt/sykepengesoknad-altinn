package no.nav.syfo

import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import no.nav.syfo.repository.SendtSoknadDao
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should be true`
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

class SendTilAltinnServiceTest : Testoppsett() {

    @Autowired
    private lateinit var sendtSoknadDao: SendtSoknadDao

    val grunnSoknad: SykepengesoknadDTO =
        objectMapper.readValue(
            Application::class.java.getResource("/arbeidstakersoknad.json"),
            SykepengesoknadDTO::class.java
        )

    @Test
    fun senderIkkeTilAltinnHvisSoknadAlleredeErSendt() {
        val soknad = grunnSoknad.copy(
            id = UUID.randomUUID().toString()
        )
        mockPdlResponse()
        mockAltinnResponse()

        leggSøknadPåKafka(soknad)
        await().atMost(Duration.ofSeconds(10)).until {
            sendtSoknadDao.soknadErSendt(soknad.id, false)
        }

        sendtSoknadDao.soknadErSendt(soknad.id, false).`should be true`()

        pdlMockWebserver.takeRequest()
        altinnMockWebserver.takeRequest()

        leggSøknadPåKafka(soknad)

        // Sendes ikke igjen
        altinnMockWebserver.takeRequest(5, TimeUnit.SECONDS).`should be null`()
    }

    @Test
    fun ettersendingTilNavBehandlesIkke() {

        val soknad = grunnSoknad.copy(
            sendtArbeidsgiver = LocalDateTime.now().minusDays(1),
            sendtNav = LocalDateTime.now(),
            ettersending = true,
            id = UUID.randomUUID().toString()
        )

        leggSøknadPåKafka(soknad)

        altinnMockWebserver.takeRequest(5, TimeUnit.SECONDS).`should be null`()
    }
/*
    @Test
    fun ettersendingTilArbeidsgiver_OK() {
        val ressursId = UUID.randomUUID().toString()

        val innsending1 = LocalDateTime.now().minusDays(1)
        val soknad1 = mockSykepengesoknad.first.copy(
            sendtArbeidsgiver = innsending1,
            sendtNav = innsending1,
            ettersending = false
        )
        sendTilAltinnService.sendSykepengesoknadTilAltinn(soknad1)
        verify(altinnConsumer, Mockito.times(1)).sendSykepengesoknadTilArbeidsgiver(any(), any())
        verify(sendtSoknadDao, Mockito.times(1)).lagreSendtSoknad(any())

        val innsending2 = LocalDateTime.now()
        val soknad2 = mockSykepengesoknad.first.copy(
            sendtArbeidsgiver = innsending2,
            sendtNav = innsending1,
            ettersending = true
        )
        sendTilAltinnService.sendSykepengesoknadTilAltinn(soknad2)
        verify(altinnConsumer, Mockito.times(2)).sendSykepengesoknadTilArbeidsgiver(any(), any())
        verify(sendtSoknadDao, Mockito.times(1)).lagreEttersendtSoknad(any(), any())
    }*/
}
