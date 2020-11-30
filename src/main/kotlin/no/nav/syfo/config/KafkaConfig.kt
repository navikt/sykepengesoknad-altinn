package no.nav.syfo.config

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.kafka.KafkaErrorHandler
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import no.nav.syfo.kafka.soknad.deserializer.FunctionDeserializer
import no.nav.syfo.kafka.soknad.serializer.FunctionSerializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE


@Configuration
@EnableKafka
class KafkaConfig(private val properties: KafkaProperties) {

    private val objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .registerModule(KotlinModule())
            .configure(READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Sykepengesoknad> = KafkaTemplate(
            DefaultKafkaProducerFactory(
                    properties.buildProducerProperties(),
                    StringSerializer(),
                    FunctionSerializer<Sykepengesoknad>(objectMapper::writeValueAsBytes)
            )
    )


    @Bean
    fun kafkaListenerContainerFactory(
            consumerFactory: ConsumerFactory<String, SykepengesoknadDTO>,
            kafkaErrorHandler: KafkaErrorHandler
    ): ConcurrentKafkaListenerContainerFactory<String, SykepengesoknadDTO> =
            ConcurrentKafkaListenerContainerFactory<String, SykepengesoknadDTO>()
                    .apply {
                        containerProperties.ackMode = MANUAL_IMMEDIATE
                        containerProperties.setErrorHandler(kafkaErrorHandler)
                        this.consumerFactory = consumerFactory
                    }

    @Bean
    fun incidentFixKafkaListenerContainerFactory(
    ): ConcurrentKafkaListenerContainerFactory<String, SykepengesoknadDTO> {

        val configs = properties.buildConsumerProperties()
        configs["auto.offset.reset"] = "earliest"
        configs[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = 1000


        val consumerFactory = DefaultKafkaConsumerFactory(
                configs,
                StringDeserializer(),
                FunctionDeserializer { bytes -> objectMapper.readValue(bytes, SykepengesoknadDTO::class.java) }
        )

        return ConcurrentKafkaListenerContainerFactory<String, SykepengesoknadDTO>()
                .apply {
                    containerProperties.ackMode = MANUAL_IMMEDIATE
                    this.consumerFactory = consumerFactory
                    this.isBatchListener = true
                }
    }

    @Bean
    @Primary
    fun consumerFactory(properties: KafkaProperties): ConsumerFactory<String, SykepengesoknadDTO> {

        return DefaultKafkaConsumerFactory(
                properties.buildConsumerProperties(),
                StringDeserializer(),
                FunctionDeserializer { bytes -> objectMapper.readValue(bytes, SykepengesoknadDTO::class.java) }
        )
    }


    @Bean
    fun kafkaListenerContainerFactoryRebehandling(
            consumerFactory: ConsumerFactory<String, Sykepengesoknad>,
            kafkaErrorHandler: KafkaErrorHandler
    ): ConcurrentKafkaListenerContainerFactory<String, Sykepengesoknad> =
            ConcurrentKafkaListenerContainerFactory<String, Sykepengesoknad>()
                    .apply {
                        containerProperties.ackMode = MANUAL_IMMEDIATE
                        containerProperties.setErrorHandler(kafkaErrorHandler)
                        this.consumerFactory = consumerFactory
                    }

    @Bean
    fun consumerFactoryRebehandling(properties: KafkaProperties): ConsumerFactory<String, Sykepengesoknad> {

        return DefaultKafkaConsumerFactory(
                properties.buildConsumerProperties(),
                StringDeserializer(),
                FunctionDeserializer { bytes -> objectMapper.readValue(bytes, Sykepengesoknad::class.java) }
        )
    }
}
