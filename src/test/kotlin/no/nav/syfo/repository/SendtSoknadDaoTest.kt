package no.nav.syfo.repository

import no.nav.syfo.LocalApplication
import no.nav.syfo.domain.SendtSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
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
        val sendtSoknad = SendtSoknad("ressursId", "altinnId", sendt, false)

        sendtSoknadDao.lagreSendtSoknad(sendtSoknad)

        val sendteSoknader = jdbcTemplate.query("SELECT * FROM SENDT_SOKNAD", sendtSoknadRowMapper)
        assertThat(sendteSoknader).hasSize(1)
        assertThat(sendteSoknader.first().ressursId).isEqualTo("ressursId")
        assertThat(sendteSoknader.first().altinnId).isEqualTo("altinnId")
        assertThat(sendteSoknader.first().sendt).isEqualTo(sendt)
        assertThat(sendteSoknader.first().ettersendt).isFalse()
    }

    @Test
    fun lagreSendtSoknadOppdatererDbVedEttersending() {
        jdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, RESSURS_ID, ALTINN_ID, SENDT, ETTERSENDT) VALUES ('1', 'ressursId', 'altinnId', date '2019-10-30', '0')")
        val sendt = LocalDateTime.now()
        val sendtSoknad = SendtSoknad("ressursId", "altinnId2", sendt, true)

        sendtSoknadDao.lagreSendtSoknad(sendtSoknad)

        val sendteSoknader = jdbcTemplate.query("SELECT * FROM SENDT_SOKNAD", sendtSoknadRowMapper)
        assertThat(sendteSoknader).hasSize(1)
        assertThat(sendteSoknader.first().ressursId).isEqualTo("ressursId")
        assertThat(sendteSoknader.first().altinnId).isEqualTo("altinnId2")
        assertThat(sendteSoknader.first().sendt).isEqualTo(sendt)
        assertThat(sendteSoknader.first().ettersendt).isTrue()
    }

    @Test
    fun soknadErSendtReturnererTrueHvisSoknadErSendt() {
        jdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, RESSURS_ID, ALTINN_ID, SENDT, ETTERSENDT) VALUES ('1', 'ressursId', 'altinnId', date '2019-10-30', '0')")

        assertThat(sendtSoknadDao.soknadErSendt("ressursId", false)).isTrue()
    }

    @Test
    fun soknadErSendtReturnererFalseHvisSoknadIkkeErSendt() {
        jdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, RESSURS_ID, ALTINN_ID, SENDT, ETTERSENDT) VALUES ('1', 'ressursId', 'altinnId', date '2019-10-30', '0')")

        assertThat(sendtSoknadDao.soknadErSendt("annenRessursId", false)).isFalse()
    }

    @Test
    fun soknadErSendtReturnererFalseHvisEttersendelseOgEttersendelseIkkeErSendt() {
        jdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, RESSURS_ID, ALTINN_ID, SENDT, ETTERSENDT) VALUES ('1', 'ressursId', 'altinnId', date '2019-10-30', '0')")

        assertThat(sendtSoknadDao.soknadErSendt("ressursId", true)).isFalse()
    }

    @Test
    fun soknadErSendtReturnererTrueHvisEttersendelseOgEttersendelseErSendt() {
        jdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, RESSURS_ID, ALTINN_ID, SENDT, ETTERSENDT) VALUES ('1', 'ressursId', 'altinnId', date '2019-10-30', '1')")

        assertThat(sendtSoknadDao.soknadErSendt("ressursId", true)).isTrue()
    }

    @Test
    fun soknadErSendtReturnererTrueHvisIkkeEttersendelseOgEttersendelseLikevelErSendt() {
        jdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, RESSURS_ID, ALTINN_ID, SENDT, ETTERSENDT) VALUES ('1', 'ressursId', 'altinnId', date '2019-10-30', '1')")

        assertThat(sendtSoknadDao.soknadErSendt("ressursId", false)).isTrue()
    }
}
