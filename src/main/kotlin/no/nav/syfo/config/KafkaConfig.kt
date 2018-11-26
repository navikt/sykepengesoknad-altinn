package no.nav.syfo.config

import io.micrometer.core.instrument.MeterRegistry
import no.nav.syfo.kafka.KafkaErrorHandler
import no.nav.syfo.kafka.sykepengesoknad.deserializer.SykepengesoknadDeserializer
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import no.nav.syfo.kafka.sykepengesoknad.serializer.SykepengesoknadSerializer
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

@Configuration
@EnableKafka
class KafkaConfig {

    @Bean
    fun kafkaListenerContainerFactory(
            consumerFactory: ConsumerFactory<String, SykepengesoknadDTO>,
            meterRegistry: MeterRegistry
    ): ConcurrentKafkaListenerContainerFactory<String, SykepengesoknadDTO> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, SykepengesoknadDTO>()
        factory.containerProperties.ackMode = AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE
        factory.containerProperties.setErrorHandler(KafkaErrorHandler(meterRegistry))
        factory.consumerFactory = consumerFactory
        return factory
    }

    @Bean
    @Profile(value = ["remote", "local-kafka"])
    @Primary
    fun consumerFactory(properties: KafkaProperties): ConsumerFactory<String, SykepengesoknadDTO> {
        return DefaultKafkaConsumerFactory(properties.buildConsumerProperties(),
                StringDeserializer(),
                SykepengesoknadDeserializer())
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, SykepengesoknadDTO>): KafkaTemplate<String, SykepengesoknadDTO> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    @Profile(value = ["remote", "local-kafka"])
    @Primary
    fun producerFactory(properties: KafkaProperties): ProducerFactory<String, SykepengesoknadDTO> {
        return DefaultKafkaProducerFactory(properties.buildProducerProperties(),
                StringSerializer(),
                SykepengesoknadSerializer())
    }
}
