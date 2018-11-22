package no.nav.syfo.controller

import no.nav.syfo.kafka.TestProducer
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/test"])
class KafkaTestController(private val testProducer: TestProducer) {

    @ResponseBody
    @RequestMapping(value = ["/produce"], produces = [MediaType.TEXT_PLAIN_VALUE])
    fun produce(): String {
        testProducer.sendMelding()

        return "Lagt testdata på topic 🚀"
    }
}
