package no.nav.syfo.repository

import no.nav.syfo.FellesTestOppsett
import no.nav.syfo.domain.SendtSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.time.Instant
import java.time.temporal.ChronoUnit

class SendtSoknadRepositoryTest : FellesTestOppsett() {
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun cleanup() {
        jdbcTemplate.update("DELETE FROM SENDT_SOKNAD")
    }

    @Test
    fun lagreSendtSoknadLagrerIDb() {
        val sendtSoknad = SendtSoknad(null, "ressursId", Instant.now())

        sendtSoknadRepository.save(sendtSoknad)

        val sendteSoknader = sendtSoknadRepository.findAll().iterator().asSequence().toList()
        assertThat(sendteSoknader).hasSize(1)
        assertThat(sendteSoknader.first().sykepengesoknadId).isEqualTo("ressursId")
        assertThat(
            sendteSoknader.first().sendt.truncatedTo(ChronoUnit.SECONDS),
        ).isEqualTo(sendtSoknad.sendt.truncatedTo(ChronoUnit.SECONDS))
    }

    @Test
    fun soknadErSendtReturnererTrueHvisSoknadErSendt() {
        jdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, SYKEPENGESOKNAD_ID, SENDT) VALUES ('1', 'ressursId', date '2019-10-30')")

        assertThat(sendtSoknadRepository.existsBySykepengesoknadId("ressursId")).isTrue()
    }

    @Test
    fun soknadErSendtReturnererFalseHvisSoknadIkkeErSendt() {
        jdbcTemplate.update("INSERT INTO SENDT_SOKNAD (ID, SYKEPENGESOKNAD_ID, SENDT) VALUES ('1', 'ressursId', date '2019-10-30')")

        assertThat(sendtSoknadRepository.existsBySykepengesoknadId("annenRessursId")).isFalse()
    }
}
