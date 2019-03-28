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
        namedParameterJdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, RESSURS_ID, ALTINN_ID, SENDT) VALUES (SENDT_SOKNAD_ID_SEQ.NEXTVAL, :ressursId, :altinnId, :sendt)",
                MapSqlParameterSource()
                        .addValue("ressursId", sendtSoknad.ressursId)
                        .addValue("altinnId", sendtSoknad.altinnId)
                        .addValue("sendt", sendtSoknad.sendt)
        )
    }

    fun lagreEttersendtSoknad(ressursId: String, altinnIdEttersending: String) {
        namedParameterJdbcTemplate.update("UPDATE SENDT_SOKNAD SET ALTINN_ID_ETTERS = :altinnIdEttersending WHERE RESSURS_ID = :ressursId",
                MapSqlParameterSource()
                        .addValue("altinnIdEttersending", altinnIdEttersending)
                        .addValue("ressursId", ressursId)
        )
    }

    fun soknadErSendt(ressursId: String, erEttersending: Boolean): Boolean {
        val sendtSoknad = namedParameterJdbcTemplate.query(
                "SELECT * FROM SENDT_SOKNAD WHERE RESSURS_ID = :ressursId",
                MapSqlParameterSource("ressursId", ressursId),
                sendtSoknadRowMapper).firstOrNull() ?: return false

        if (erEttersending && sendtSoknad.altinnIdEttersending == null) {
            return false
        }

        if (!erEttersending && sendtSoknad.altinnIdEttersending != null) {
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
            resultSet.getString("ALTINN_ID_ETTERS")
    )
}
