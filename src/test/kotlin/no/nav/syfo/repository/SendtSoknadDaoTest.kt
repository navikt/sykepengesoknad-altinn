package no.nav.syfo.repository

import no.nav.syfo.LocalApplication
import no.nav.syfo.domain.SendtSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDateTime
import javax.inject.Inject

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SendtSoknadDaoTest {

    @Inject
    private lateinit var jdbcTemplate: JdbcTemplate

    @Inject
    private lateinit var sendtSoknadDao: SendtSoknadDao

    @Before
    fun cleanup() {
        jdbcTemplate.update("DELETE FROM SENDT_SOKNAD")
    }

    @Test
    fun lagreSendtSoknadLagrerIDb() {
        val sendt = LocalDateTime.now()
        val sendtSoknad = SendtSoknad("ressursId", "altinnId", sendt)

        sendtSoknadDao.lagreSendtSoknad(sendtSoknad)

        val sendteSoknader = jdbcTemplate.query("SELECT * FROM SENDT_SOKNAD", sendtSoknadRowMapper)
        assertThat(sendteSoknader).hasSize(1)
        assertThat(sendteSoknader.first().ressursId).isEqualTo("ressursId")
        assertThat(sendteSoknader.first().altinnId).isEqualTo("altinnId")
        assertThat(sendteSoknader.first().sendt).isEqualTo(sendt)
    }

    @Test
    fun soknadErSendtReturnererTrueHvisSoknadErSendt() {
        jdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, RESSURS_ID, ALTINN_ID, SENDT) VALUES ('1', 'ressursId', 'altinnId', date '2019-10-30')")

        assertThat(sendtSoknadDao.soknadErSendt("ressursId")).isTrue()
    }

    @Test
    fun soknadErSendtReturnererFalseHvisSoknadIkkeErSendt() {
        jdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, RESSURS_ID, ALTINN_ID, SENDT) VALUES ('1', 'ressursId', 'altinnId', date '2019-10-30')")

        assertThat(sendtSoknadDao.soknadErSendt("annenRessursId")).isFalse()
    }
}

val sendtSoknadRowMapper = RowMapper { resultSet, _ ->
    SendtSoknad(
            resultSet.getString("RESSURS_ID"),
            resultSet.getString("ALTINN_ID"),
            resultSet.getTimestamp("SENDT").toLocalDateTime()
    )
}
