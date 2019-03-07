package no.nav.syfo.repository

import no.nav.syfo.domain.SendtSoknad
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

    fun lagreSendtSoknad(sendtSoknad: SendtSoknad){
        namedParameterJdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, RESSURS_ID, ALTINN_ID, SENDT) VALUES (SENDT_SOKNAD_ID_SEQ.NEXTVAL, :ressursId, :altinnId, :sendt)",
        MapSqlParameterSource()
                .addValue("ressursId", sendtSoknad.ressursId)
                .addValue("altinnId", sendtSoknad.altinnId)
                .addValue("sendt", sendtSoknad.sendt)
        )
    }

    fun soknadErSendt(ressursId: String): Boolean {
        return namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM SENDT_SOKNAD WHERE RESSURS_ID = :ressursId",
                MapSqlParameterSource("ressursId", ressursId),
                Boolean::class.java
        ) ?: false
    }
}

val sendtSoknadRowMapper = RowMapper { resultSet, _ ->
    SendtSoknad(
            id = resultSet.getString("ID"),
            ressursId = resultSet.getString("RESSURS_ID"),
            altinnId = resultSet.getString("ALTINN_ID"),
            sendt = resultSet.getTimestamp("SENDT").toLocalDateTime()
    )
}
