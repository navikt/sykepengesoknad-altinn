package no.nav.syfo.domain

import java.time.LocalDateTime

data class SendtSoknad(
        val id: String? = null,
        val ressursId: String,
        val altinnId: String,
        val sendt: LocalDateTime
)
