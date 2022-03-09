package no.nav.syfo.repository

import no.nav.syfo.domain.SendtSoknad
import no.nav.syfo.logger
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@Repository
class SendtSoknadDao(internal val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    val log = logger()

    fun lagreSendtSoknad(sendtSoknad: SendtSoknad) {
        namedParameterJdbcTemplate.update(
            "INSERT INTO SENDT_SOKNAD ( SYKEPENGESOKNAD_ID, SENDT) VALUES (:ressursId, :sendt)",
            MapSqlParameterSource()
                .addValue("ressursId", sendtSoknad.ressursId)
                .addValue("sendt", sendtSoknad.sendt)
        )
    }

    fun soknadErSendt(ressursId: String): Boolean {
        namedParameterJdbcTemplate.query(
            "SELECT * FROM SENDT_SOKNAD WHERE SYKEPENGESOKNAD_ID = :ressursId",
            MapSqlParameterSource("ressursId", ressursId),
            sendtSoknadRowMapper
        ).firstOrNull() ?: return false

        return true
    }
}

val sendtSoknadRowMapper = RowMapper { resultSet, _ ->
    SendtSoknad(
        resultSet.getString("SYKEPENGESOKNAD_ID"),
        resultSet.getTimestamp("SENDT").toLocalDateTime(),
    )
}
