package no.nav.syfo.selftest

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val APPLICATION_LIVENESS = "Application is alive!"
const val APPLICATION_READY = "Application is ready!"

@RestController
@RequestMapping(value = ["/internal"])
class SelftestController(private val registry: MeterRegistry) {

    val isAlive: ResponseEntity<String>
        @GetMapping(value = ["/isAlive"], produces = [MediaType.TEXT_PLAIN_VALUE])
        get() = if (registry.counter("syfoaltinn.kafka.feil", Tags.of("type", "fatale")).count() > 2.0) {
            ResponseEntity("Feil: for mange fatale kafkafeil", HttpStatus.INTERNAL_SERVER_ERROR)
        } else {
            ResponseEntity.ok(APPLICATION_LIVENESS)
        }

    val isReady: String
        @GetMapping(value = ["/isReady"], produces = [MediaType.TEXT_PLAIN_VALUE])
        get() = APPLICATION_READY
}
