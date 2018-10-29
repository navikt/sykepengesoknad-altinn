package no.nav.syfo.repository

import no.nav.syfo.domain.Car
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.util.*

@Service
@Transactional
@Repository
class CarDAO(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    val innsendingRowMapper: (ResultSet, Int) -> Car = { resultSet, _ ->
        Car(
            uuid = (resultSet.getString("CAR_UUID")),
            color = (resultSet.getString("COLOR")),
            brand = (resultSet.getString("BRAND")),
            bought = (resultSet.getDate("BOUGHT").toLocalDate())
        )
    }

    fun lagreCar(car: Car): String {
        val uuid = UUID.randomUUID().toString()

        namedParameterJdbcTemplate.update(
            "INSERT INTO CAR VALUES(:uuid, :color, :brand, :bought)",
            MapSqlParameterSource()
                .addValue("uuid", uuid)
                .addValue("color", car.color)
                .addValue("brand", car.brand)
                .addValue("bought", car.bought)
        )

        return uuid
    }

    fun hentCars(): List<Car> {
        return namedParameterJdbcTemplate.query("SELECT * FROM CAR", innsendingRowMapper)
    }
}
