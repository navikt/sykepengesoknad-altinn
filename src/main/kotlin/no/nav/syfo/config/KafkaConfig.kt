package no.nav.syfo.config

import com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.core.instrument.MeterRegistry
import no.nav.syfo.kafka.KafkaErrorHandler
import no.nav.syfo.kafka.LegacyMultiFunctionDeserializer
import no.nav.syfo.kafka.MELDINGSTYPE
import no.nav.syfo.kafka.getLastHeaderByKeyAsString
import no.nav.syfo.kafka.interfaces.Soknad
import no.nav.syfo.kafka.soknad.dto.SoknadDTO
import no.nav.syfo.kafka.soknad.serializer.FunctionSerializer
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import no.nav.syfo.selftest.ApplicationState
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
import org.springframework.kafka.listener.adapter.RecordFilterStrategy


@Configuration
@EnableKafka
class KafkaConfig {

    private val objectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .configure(READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)

    @Bean
    fun recordFilterStrategy(): RecordFilterStrategy<String, Soknad> {
        return RecordFilterStrategy { consumerRecord ->
            "SYKEPENGESOKNAD" != getLastHeaderByKeyAsString(consumerRecord.headers(), MELDINGSTYPE)
        }
    }

    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, Soknad>,
        meterRegistry: MeterRegistry,
        applicationState: ApplicationState,
        recordFilterStrategy: RecordFilterStrategy<String, Soknad>
    ): ConcurrentKafkaListenerContainerFactory<String, Soknad> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Soknad>()
        factory.containerProperties.ackMode = AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE
        factory.containerProperties.setErrorHandler(KafkaErrorHandler(meterRegistry, applicationState))
        factory.consumerFactory = consumerFactory
        factory.setRecordFilterStrategy(recordFilterStrategy)
        return factory
    }

    @Bean
    @Profile(value = ["remote", "local-kafka"])
    @Primary
    fun consumerFactory(properties: KafkaProperties): ConsumerFactory<String, Soknad> {
        return DefaultKafkaConsumerFactory(properties.buildConsumerProperties(),
            StringDeserializer(),
            LegacyMultiFunctionDeserializer<Soknad>(
                mapOf(
                    "SYKEPENGESOKNAD" to { _, bytes -> bytes?.let { objectMapper.readValue<SykepengesoknadDTO>(it) } as Soknad },
                    "SOKNAD" to { _, bytes -> bytes?.let { objectMapper.readValue<SoknadDTO>(it) } as Soknad }
                )
            )
        )
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
