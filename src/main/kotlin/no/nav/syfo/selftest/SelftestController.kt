package no.nav.syfo.selftest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.syfo.SykepengesoknadAltinn
import no.nav.syfo.consumer.ws.client.AltinnConsumer
import no.nav.syfo.kafka.konverter
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import javax.inject.Inject

const val APPLICATION_LIVENESS = "Application is alive!"
const val APPLICATION_READY = "Application is ready!"

@RestController
@RequestMapping(value = ["/internal"])
class SelftestController @Inject
constructor(private val altinnConsumer: AltinnConsumer) {

    val isAlive: String
        @ResponseBody
        @RequestMapping(value = ["/isAlive"], produces = [MediaType.TEXT_PLAIN_VALUE])
        get() {
            System.out.println("Skal sende test-søknad til altinn")

            val objectMapper = ObjectMapper().registerModule(JavaTimeModule())

            val readInputStream = SelftestController::class.java.getResourceAsStream("/soknad.json")
            val sykepengesoknadDTO = objectMapper.readValue(readInputStream, SykepengesoknadDTO::class.java)
            val sykepengesoknad = konverter(sykepengesoknadDTO)

            val sendSykepengesoknadTilArbeidsgiver = altinnConsumer.sendSykepengesoknadTilArbeidsgiver(SykepengesoknadAltinn(sykepengesoknad))

            System.out.println("Dette er responsen fra altinn: " + sendSykepengesoknadTilArbeidsgiver)

            return APPLICATION_LIVENESS
        }

    val isReady: String
        @ResponseBody
        @RequestMapping(value = ["/isReady"], produces = [MediaType.TEXT_PLAIN_VALUE])
        get() = APPLICATION_READY
}
