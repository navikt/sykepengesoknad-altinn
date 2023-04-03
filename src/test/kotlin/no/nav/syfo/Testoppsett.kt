package no.nav.syfo

import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.syfo.domain.SykmeldingKafkaMessage
import no.nav.syfo.egenmelding.EgenmeldingFraSykmeldingRepository
import no.nav.syfo.kafka.SYKEPENGESOKNAD_TOPIC
import no.nav.syfo.orgnummer.JuridiskOrgnummerRepository
import no.nav.syfo.orgnummer.SYKMELDINGSENDT_TOPIC
import no.nav.syfo.repository.SendtSoknadRepository
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

private class PostgreSQLContainer12 : PostgreSQLContainer<PostgreSQLContainer12>("postgres:12-alpine")

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@EnableMockOAuth2Server
abstract class Testoppsett {

    companion object {
        var altinnMockWebserver: MockWebServer
        var pdlMockWebserver: MockWebServer
        var pdfMockWebserver: MockWebServer

        init {
            KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.2")).also {
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

            altinnMockWebserver = MockWebServer()
                .also { it.start() }
                .also {
                    System.setProperty("altinn.url", "http://localhost:${it.port}")
                }

            pdlMockWebserver = MockWebServer()
                .also {
                    System.setProperty("PDL_URL", "http://localhost:${it.port}")
                }

            pdfMockWebserver = MockWebServer()
                .also {
                    it.dispatcher = object : okhttp3.mockwebserver.Dispatcher() {
                        override fun dispatch(request: okhttp3.mockwebserver.RecordedRequest): MockResponse =
                            MockResponse().setResponseCode(200).setBody("PDF")
                    }
                    System.setProperty("pdfgen.url", "http://localhost:${it.port}")
                }
        }
    }

    @Autowired
    lateinit var aivenKafkaProducer: KafkaProducer<String, String>

    @Autowired
    lateinit var sendtSoknadRepository: SendtSoknadRepository

    @Autowired
    lateinit var juridiskOrgnummerRepository: JuridiskOrgnummerRepository

    @Autowired
    lateinit var egenmeldingFraSykmeldingRepository: EgenmeldingFraSykmeldingRepository

    @AfterEach
    internal fun tearDown() {
        juridiskOrgnummerRepository.deleteAll()
        sendtSoknadRepository.deleteAll()
        egenmeldingFraSykmeldingRepository.deleteAll()
    }

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

    fun leggSykmeldingPåKafka(sykmeldingKafkaMessage: SykmeldingKafkaMessage) {
        aivenKafkaProducer.send(
            ProducerRecord(
                SYKMELDINGSENDT_TOPIC,
                null,
                sykmeldingKafkaMessage.kafkaMetadata.sykmeldingId,
                sykmeldingKafkaMessage.serialisertTilString()
            )
        )
    }
}
