package no.nav.syfo.domain

import java.time.LocalDateTime

data class SendtSoknad(
        val ressursId: String,
        val altinnId: String,
        val sendt: LocalDateTime,
        val altinnIdEttersending: String? = null
)
