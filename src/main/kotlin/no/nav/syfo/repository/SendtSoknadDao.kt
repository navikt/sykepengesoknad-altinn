package no.nav.syfo.repository

import no.nav.syfo.domain.SendtSoknad
import no.nav.syfo.log
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

    val log = log()

    fun lagreSendtSoknad(sendtSoknad: SendtSoknad) {
        if (sendtSoknad.ettersendt) {
            namedParameterJdbcTemplate.update("UPDATE SENDT_SOKNAD SET ALTINN_ID = :altinnId, SENDT = :sendt, ETTERSENDT = :ettersendt WHERE RESSURS_ID = :ressursId",
                    MapSqlParameterSource()
                            .addValue("altinnId", sendtSoknad.altinnId)
                            .addValue("sendt", sendtSoknad.sendt)
                            .addValue("ettersendt", 1)
                            .addValue("ressursId", sendtSoknad.ressursId)
            )
        } else {
            namedParameterJdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, RESSURS_ID, ALTINN_ID, SENDT, ETTERSENDT) VALUES (SENDT_SOKNAD_ID_SEQ.NEXTVAL, :ressursId, :altinnId, :sendt, :ettersendt)",
                    MapSqlParameterSource()
                            .addValue("ressursId", sendtSoknad.ressursId)
                            .addValue("altinnId", sendtSoknad.altinnId)
                            .addValue("sendt", sendtSoknad.sendt)
                            .addValue("ettersendt", 0)
            )
        }
    }

    fun soknadErSendt(ressursId: String, erEttersending: Boolean): Boolean {
        val sendtSoknad = namedParameterJdbcTemplate.query(
                "SELECT * FROM SENDT_SOKNAD WHERE RESSURS_ID = :ressursId",
                MapSqlParameterSource("ressursId", ressursId),
                sendtSoknadRowMapper).firstOrNull() ?: return false

        if (erEttersending && !sendtSoknad.ettersendt) {
            return false
        }

        if (!erEttersending && sendtSoknad.ettersendt) {
            log.error("Forsøker å sende søknad med id {} som allerede har blitt ettersendt, avbryter..", sendtSoknad.ressursId)
        }
        return true
    }
}

val sendtSoknadRowMapper = RowMapper { resultSet, _ ->
    SendtSoknad(
            resultSet.getString("RESSURS_ID"),
            resultSet.getString("ALTINN_ID"),
            resultSet.getTimestamp("SENDT").toLocalDateTime(),
            resultSet.getInt("ETTERSENDT") != 0
    )
}
