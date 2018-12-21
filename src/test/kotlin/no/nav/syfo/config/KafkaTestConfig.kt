package no.nav.syfo.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.syfo.kafka.interfaces.Soknad
import no.nav.syfo.kafka.soknad.deserializer.MultiFunctionDeserializer
import no.nav.syfo.kafka.soknad.serializer.FunctionSerializer
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.ClassRule
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.test.rule.KafkaEmbedded
import java.util.*
import java.util.function.BiFunction
import java.util.function.Function

@Configuration
@EnableKafka
class KafkaTestConfig {

    companion object {
        @ClassRule
        var embeddedKafka = KafkaEmbedded(1, true, "test")
    }

    private val objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)

    @Bean
    fun consumerFactory(properties: KafkaProperties): ConsumerFactory<String, Soknad> {
        return DefaultKafkaConsumerFactory(properties.buildConsumerProperties(),
                StringDeserializer(),
                MultiFunctionDeserializer(Collections.singletonMap("SYKEPENGESOKNAD",
                        BiFunction { _, byteArray ->
                            objectMapper.readValue(byteArray, SykepengesoknadDTO::class.java)
                        }
                ), Function {
                    null
                }))
    }

    @Bean
    fun producerFactory(properties: KafkaProperties): ProducerFactory<String, Soknad> {
        return DefaultKafkaProducerFactory(properties.buildProducerProperties(),
                StringSerializer(),
                FunctionSerializer { soknad -> objectMapper.writeValueAsBytes(soknad) }
        )
    }
}
