package no.nav.syfo

import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.syfo.kafka.SYKEPENGESOKNAD_TOPIC
import okhttp3.mockwebserver.MockWebServer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

private class PostgreSQLContainer12 : PostgreSQLContainer<PostgreSQLContainer12>("postgres:12-alpine")
private class PdfGenerator :
    GenericContainer<PdfGenerator>("ghcr.io/navikt/flex-sykepengesoknad-pdfgen/flex-sykepengesoknad-pdfgen:2022.03.08-07.39-85951878")

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@EnableMockOAuth2Server
abstract class Testoppsett {

    companion object {
        var altinnMockWebserver: MockWebServer
        var pdlMockWebserver: MockWebServer
        init {
            KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.1.0")).also {
                it.start()
                System.setProperty("spring.kafka.bootstrap-servers", it.bootstrapServers)
                System.setProperty("KAFKA_BROKERS", it.bootstrapServers)
            }
            PostgreSQLContainer12().also {
                it.start()
                System.setProperty("spring.datasource.url", "${it.jdbcUrl}&reWriteBatchedInserts=true")
                System.setProperty("spring.datasource.username", it.username)
                System.setProperty("spring.datasource.password", it.password)
            }
            PdfGenerator().withExposedPorts(8080).also {
                it.start()
                System.setProperty("pdfgen.url", "http://${it.host}:${it.firstMappedPort}")
            }
            altinnMockWebserver = MockWebServer()
                .also { it.start() }
                .also {
                    System.setProperty("altinn.url", "http://localhost:${it.port}")
                }

            pdlMockWebserver = MockWebServer()
                .also {
                    System.setProperty("pdl.api.url", "http://localhost:${it.port}")
                }
        }
    }

    @Autowired
    lateinit var aivenKafkaProducer: KafkaProducer<String, String>

    fun leggSøknadPåKafka(soknad: SykepengesoknadDTO) {
        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                null,
                soknad.id,
                soknad.serialisertTilString()
            )
        )
    }
}
