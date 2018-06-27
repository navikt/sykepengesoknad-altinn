package no.nav.syfo.repository;

import no.nav.syfo.LocalApplication;
import no.nav.syfo.domain.Car;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public class CarDAOTest {

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private CarDAO carDAO;

    @Before
    public void cleanup() {
        jdbcTemplate.update("DELETE FROM CAR");
    }

    @Test
    public void hentCars() {
        jdbcTemplate.update("INSERT INTO CAR VALUES('id', 'blue', 'Lada', date '2018-05-05')");

        List<Car> cars = carDAO.hentCars();

    }

    @Test
    public void lagreCar() {
        Car car = Car.builder()
                .color("red")
                .brand("Mercedes")
                .bought(LocalDate.of(2018, 6, 27))
                .build();
        carDAO.lagreCar(car);

        List<Car> cars = jdbcTemplate.query("SELECT * FROM CAR", CarDAO.getInnsendingRowMapper());
        assertThat(cars.size()).isEqualTo(1);
        assertThat(cars.get(0).getColor()).isEqualTo("red");
    }
}