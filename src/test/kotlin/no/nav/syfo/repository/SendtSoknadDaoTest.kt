package no.nav.syfo.repository

import no.nav.syfo.Testoppsett
import no.nav.syfo.domain.SendtSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class SendtSoknadDaoTest : Testoppsett() {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var sendtSoknadDao: SendtSoknadDao

    @BeforeEach
    fun cleanup() {
        jdbcTemplate.update("DELETE FROM SENDT_SOKNAD")
    }

    @Test
    fun lagreSendtSoknadLagrerIDb() {
        val sendt = LocalDateTime.now()
        val sendtSoknad = SendtSoknad("ressursId", sendt)

        sendtSoknadDao.lagreSendtSoknad(sendtSoknad)

        val sendteSoknader = jdbcTemplate.query("SELECT * FROM SENDT_SOKNAD", sendtSoknadRowMapper)
        assertThat(sendteSoknader).hasSize(1)
        assertThat(sendteSoknader.first().ressursId).isEqualTo("ressursId")
        assertThat(sendteSoknader.first().sendt.truncatedTo(ChronoUnit.SECONDS)).isEqualTo(sendt.truncatedTo(ChronoUnit.SECONDS))
    }

    @Test
    fun soknadErSendtReturnererTrueHvisSoknadErSendt() {
        jdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, SYKEPENGESOKNAD_ID, SENDT) VALUES ('1', 'ressursId', date '2019-10-30')")

        assertThat(sendtSoknadDao.soknadErSendt("ressursId")).isTrue()
    }

    @Test
    fun soknadErSendtReturnererFalseHvisSoknadIkkeErSendt() {
        jdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, SYKEPENGESOKNAD_ID, SENDT) VALUES ('1', 'ressursId', date '2019-10-30')")

        assertThat(sendtSoknadDao.soknadErSendt("annenRessursId")).isFalse()
    }
}
