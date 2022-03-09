package no.nav.syfo.repository

import no.nav.syfo.domain.SendtSoknad
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SendtSoknadRepository : CrudRepository<SendtSoknad, String> {
    fun existsBySykepengesoknadId(sykepengesoknadId: String): Boolean
}
