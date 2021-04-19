package no.nav.syfo

import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
abstract class AbstractContainerBaseTest {

    companion object {
        init {

            KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.1.0")).also {
                it.start()
                System.setProperty("spring.kafka.bootstrap-servers", it.bootstrapServers)
                System.setProperty("KAFKA_BROKERS", it.bootstrapServers)
            }
        }
    }
}
