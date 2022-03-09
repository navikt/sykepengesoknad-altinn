package no.nav.syfo.domain

import java.time.LocalDateTime

data class SendtSoknad(
    val ressursId: String,
    val sendt: LocalDateTime,
)
