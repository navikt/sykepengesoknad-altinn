package no.nav.syfo.import

import no.nav.syfo.domain.SendtSoknad
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.PreparedStatement
import java.sql.Timestamp

@Repository
class BatchInsertDAO(
    private val jdbcTemplate: JdbcTemplate
) {

    fun batchInsertInnsending(records: List<SendtSoknad>): IntArray {

        val sql = """
INSERT INTO SENDT_SOKNAD 
    ("sykepengesoknad_id",
    "sendt")
VALUES
  (?,?) ON CONFLICT ON CONSTRAINT sendt_soknad_sykepengesoknad_id_key DO NOTHING;"""

        return jdbcTemplate.batchUpdate(
            sql,
            object : BatchPreparedStatementSetter {
                override fun setValues(ps: PreparedStatement, i: Int) {
                    ps.setString(1, records[i].sykepengesoknadId)
                    ps.setTimestamp(2, Timestamp.from(records[i].sendt))
                }

                override fun getBatchSize() = records.size
            }
        )
    }
}
