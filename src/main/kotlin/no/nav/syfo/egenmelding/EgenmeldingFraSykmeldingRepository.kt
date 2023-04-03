package no.nav.syfo.egenmelding

import org.springframework.data.annotation.Id
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface EgenmeldingFraSykmeldingRepository : CrudRepository<EgenmeldingFraSykmelding, String> {
    fun findBySykmeldingId(sykmeldingId: String): EgenmeldingFraSykmelding?
}

data class EgenmeldingFraSykmelding(
    @Id
    val id: String? = null,
    val sykmeldingId: String,
    val egenmeldingssvar: String
)
