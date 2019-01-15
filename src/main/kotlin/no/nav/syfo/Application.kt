package no.nav.syfo

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

const val CALL_ID = "callId"
const val BEHANDLINGSTIDSPUNKT = "behandlingstidspunkt"

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

inline fun <reified T> T.log(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}