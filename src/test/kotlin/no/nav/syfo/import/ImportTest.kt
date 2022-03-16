package no.nav.syfo.import

import no.nav.syfo.Testoppsett
import no.nav.syfo.serialisertTilString
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldHaveSize
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.record.TimestampType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.support.Acknowledgment
import org.springframework.test.annotation.DirtiesContext
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

@DirtiesContext
class ImportTest : Testoppsett() {

    @MockBean
    lateinit var acknowledgment: Acknowledgment

    @Autowired
    lateinit var innsendingImportListener: ImportListener

    @BeforeEach
    fun setup() {
        sendtSoknadRepository.deleteAll()
    }

    @Test
    fun `Mottar innsending fra kafka`() {
        sendtSoknadRepository.findAll().iterator().asSequence().toList().shouldBeEmpty()

        val innsending = SendtSoknadKafkaDto(
            sykepengesoknadId = "1234",

            sendt = OffsetDateTime.now()
        )
        val innsending2 = SendtSoknadKafkaDto(
            sykepengesoknadId = "123465",

            sendt = OffsetDateTime.now()
        )
        leggInnsendingPaKafka(innsending)
        leggInnsendingPaKafka(innsending2)

        val records = sendtSoknadRepository.findAll().iterator().asSequence().toList()
        records.shouldHaveSize(2)
        records.first { it.sykepengesoknadId == innsending.sykepengesoknadId }.sykepengesoknadId `should be equal to` innsending.sykepengesoknadId
        records.first { it.sykepengesoknadId == innsending.sykepengesoknadId }.sendt.truncatedTo(ChronoUnit.SECONDS) `should be equal to` innsending.sendt.toInstant()
            .truncatedTo(ChronoUnit.SECONDS)
    }

    private fun leggInnsendingPaKafka(innsending: SendtSoknadKafkaDto) =
        innsendingImportListener.listen(
            listOf(skapConsumerRecord(innsending.sykepengesoknadId, innsending.serialisertTilString())),
            acknowledgment
        )

    fun <T> skapConsumerRecord(key: String, value: T, headers: Headers = RecordHeaders()): ConsumerRecord<String, T> {
        @Suppress("DEPRECATION")
        return ConsumerRecord(
            "topic-v1",
            0,
            0,
            0,
            TimestampType.CREATE_TIME,
            0,
            0,
            0,
            key,
            value,
            headers
        )
    }
}
