package no.nav.syfo.config

import com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.micrometer.core.instrument.MeterRegistry
import no.nav.syfo.kafka.KafkaErrorHandler
import no.nav.syfo.kafka.interfaces.Soknad
import no.nav.syfo.kafka.soknad.deserializer.MultiFunctionDeserializer
import no.nav.syfo.kafka.soknad.serializer.FunctionSerializer
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.AbstractMessageListenerContainer
import java.util.Collections.singletonMap
import java.util.function.BiFunction
import java.util.function.Function

@Configuration
@EnableKafka
class KafkaConfig {

    private val objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .configure(READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)

    @Bean
    fun kafkaListenerContainerFactory(
            consumerFactory: ConsumerFactory<String, Soknad>,
            meterRegistry: MeterRegistry
    ): ConcurrentKafkaListenerContainerFactory<String, Soknad> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Soknad>()
        factory.containerProperties.ackMode = AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE
        factory.containerProperties.setErrorHandler(KafkaErrorHandler(meterRegistry))
        factory.consumerFactory = consumerFactory
        return factory
    }

    @Bean
    @Profile(value = ["remote", "local-kafka"])
    @Primary
    fun consumerFactory(properties: KafkaProperties): ConsumerFactory<String, Soknad> {
        return DefaultKafkaConsumerFactory(properties.buildConsumerProperties(),
                StringDeserializer(),
                MultiFunctionDeserializer(singletonMap("SYKEPENGESOKNAD",
                        BiFunction { _, byteArray ->
                            objectMapper.readValue(byteArray, SykepengesoknadDTO::class.java)
                        }
                ), Function {
                    null
                }))
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, Soknad>): KafkaTemplate<String, Soknad> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    @Profile(value = ["remote", "local-kafka"])
    @Primary
    fun producerFactory(properties: KafkaProperties): ProducerFactory<String, Soknad> {
        return DefaultKafkaProducerFactory(properties.buildProducerProperties(),
                StringSerializer(),
                FunctionSerializer { soknad -> objectMapper.writeValueAsBytes(soknad) }
        )
    }
}
