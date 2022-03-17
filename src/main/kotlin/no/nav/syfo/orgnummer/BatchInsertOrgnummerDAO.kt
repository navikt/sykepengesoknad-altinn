package no.nav.syfo.orgnummer

import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.PreparedStatement

@Repository
class BatchInsertOrgnummerDAO(
    private val jdbcTemplate: JdbcTemplate
) {

    fun batchInsertOrgnummer(records: List<JuridiskOrgnummer>): IntArray {

        val sql = """
INSERT INTO JURIDISK_ORGNUMMER 
    ("sykmelding_id",
    "orgnummer",
    "juridisk_orgnummer")
VALUES
  (?,?,?) ON CONFLICT ON CONSTRAINT juridisk_orgnummer_sykmelding_id_key DO NOTHING;"""

        return jdbcTemplate.batchUpdate(
            sql,
            object : BatchPreparedStatementSetter {
                override fun setValues(ps: PreparedStatement, i: Int) {
                    ps.setString(1, records[i].sykmeldingId)
                    ps.setString(2, records[i].orgnummer)
                    ps.setString(3, records[i].juridiskOrgnummer)
                }

                override fun getBatchSize() = records.size
            }
        )
    }
}
