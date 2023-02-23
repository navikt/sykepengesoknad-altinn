package no.nav.syfo.domain

import org.springframework.data.annotation.Id
import java.time.Instant

data class SendtSoknad(
    @Id
    val id: String? = null,
    val sykepengesoknadId: String,
    val sendt: Instant
)
