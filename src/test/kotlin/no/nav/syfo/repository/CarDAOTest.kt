package no.nav.syfo.repository

import no.nav.syfo.LocalApplication
import no.nav.syfo.domain.Car
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate
import javax.inject.Inject

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
class CarDAOTest {

    @Inject
    private val jdbcTemplate: JdbcTemplate? = null

    @Inject
    private val carDAO: CarDAO? = null

    @Before
    fun cleanup() {
        jdbcTemplate!!.update("DELETE FROM CAR")
    }

    @Test
    fun hentCars() {
        jdbcTemplate!!.update("INSERT INTO CAR VALUES('id', 'blue', 'Lada', date '2018-05-05')")

        val cars = carDAO!!.hentCars()
    }

    @Test
    fun lagreCar() {
        val car = Car(
            color = "red",
            brand = "Mercedes",
            bought = LocalDate.of(2018, 6, 27)
        )
        carDAO!!.lagreCar(car)

        val cars = jdbcTemplate!!.query("SELECT * FROM CAR", carDAO.innsendingRowMapper)
        assertThat(cars).hasSize(1)
        assertThat(cars.first().color).isEqualTo("red")
    }
}