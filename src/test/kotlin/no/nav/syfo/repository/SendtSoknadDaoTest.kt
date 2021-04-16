package no.nav.syfo.repository

import no.nav.syfo.LocalApplication
import no.nav.syfo.domain.SendtSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDateTime
import javax.inject.Inject


@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka
class SendtSoknadDaoTest {

    @Inject
    private lateinit var jdbcTemplate: JdbcTemplate

    @Inject
    private lateinit var sendtSoknadDao: SendtSoknadDao

    @BeforeEach
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
        assertThat(sendteSoknader.first().altinnIdEttersending).isNull()
    }

    @Test
    fun lagreEttersendtSoknadOppdatererDbVedEttersending() {
        jdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, RESSURS_ID, ALTINN_ID, SENDT) VALUES ('1', 'ressursId', 'altinnId', date '2019-10-30')")

        sendtSoknadDao.lagreEttersendtSoknad("ressursId", "altinnId2")

        val sendteSoknader = jdbcTemplate.query("SELECT * FROM SENDT_SOKNAD", sendtSoknadRowMapper)
        assertThat(sendteSoknader).hasSize(1)
        assertThat(sendteSoknader.first().ressursId).isEqualTo("ressursId")
        assertThat(sendteSoknader.first().altinnId).isEqualTo("altinnId")
        assertThat(sendteSoknader.first().altinnIdEttersending).isEqualTo("altinnId2")
    }

    @Test
    fun soknadErSendtReturnererTrueHvisSoknadErSendt() {
        jdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, RESSURS_ID, ALTINN_ID, SENDT) VALUES ('1', 'ressursId', 'altinnId', date '2019-10-30')")

        assertThat(sendtSoknadDao.soknadErSendt("ressursId", false)).isTrue()
    }

    @Test
    fun soknadErSendtReturnererFalseHvisSoknadIkkeErSendt() {
        jdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, RESSURS_ID, ALTINN_ID, SENDT) VALUES ('1', 'ressursId', 'altinnId', date '2019-10-30')")

        assertThat(sendtSoknadDao.soknadErSendt("annenRessursId", false)).isFalse()
    }

    @Test
    fun soknadErSendtReturnererFalseHvisEttersendelseOgEttersendelseIkkeErSendt() {
        jdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, RESSURS_ID, ALTINN_ID, SENDT) VALUES ('1', 'ressursId', 'altinnId', date '2019-10-30')")

        assertThat(sendtSoknadDao.soknadErSendt("ressursId", true)).isFalse()
    }

    @Test
    fun soknadErSendtReturnererTrueHvisEttersendelseOgEttersendelseErSendt() {
        jdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, RESSURS_ID, ALTINN_ID, SENDT, ALTINN_ID_ETTERS) VALUES ('1', 'ressursId', 'altinnId', date '2019-10-30', 'altinnId2')")

        assertThat(sendtSoknadDao.soknadErSendt("ressursId", true)).isTrue()
    }

    @Test
    fun soknadErSendtReturnererTrueHvisIkkeEttersendelseOgEttersendelseLikevelErSendt() {
        jdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, RESSURS_ID, ALTINN_ID, SENDT, ALTINN_ID_ETTERS) VALUES ('1', 'ressursId', 'altinnId', date '2019-10-30', 'altinnId2')")

        assertThat(sendtSoknadDao.soknadErSendt("ressursId", false)).isTrue()
    }
}
