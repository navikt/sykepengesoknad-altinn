package no.nav.syfo.orgnummer

import org.springframework.data.annotation.Id
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface JuridiskOrgnummerRepository : CrudRepository<JuridiskOrgnummer, String> {
    fun findBySykmeldingId(sykmeldingId: String): JuridiskOrgnummer?
}

data class JuridiskOrgnummer(
    @Id
    val id: String? = null,
    val sykmeldingId: String,
    val orgnummer: String,
    val juridiskOrgnummer: String?
)
