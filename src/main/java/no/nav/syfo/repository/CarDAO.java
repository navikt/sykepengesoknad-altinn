package no.nav.syfo.repository;

import no.nav.syfo.domain.Car;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@Repository
public class CarDAO {

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public CarDAO(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public String lagreCar(final Car car) {
        String uuid = UUID.randomUUID().toString();

        namedParameterJdbcTemplate.update(
                "INSERT INTO CAR VALUES(:uuid, :color, :brand, :bought)",
                new MapSqlParameterSource()
                        .addValue("uuid", uuid)
                        .addValue("color", car.getColor())
                        .addValue("brand", car.getBrand())
                        .addValue("bought", car.getBought())
        );

        return uuid;
    }

    public List<Car> hentCars() {
        return namedParameterJdbcTemplate.query("SELECT * FROM CAR", getInnsendingRowMapper());
    }

    public static RowMapper<Car> getInnsendingRowMapper() {
        return (resultSet, i) -> Car.builder()
                .uuid(resultSet.getString("CAR_UUID"))
                .color(resultSet.getString("COLOR"))
                .brand(resultSet.getString("BRAND"))
                .bought(resultSet.getDate("BOUGHT").toLocalDate())
                .build();
    }
}
