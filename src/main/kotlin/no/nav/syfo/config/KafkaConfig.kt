package no.nav.syfo.config

import io.micrometer.core.instrument.MeterRegistry
import no.nav.syfo.kafka.KafkaErrorHandler
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*

@Configuration
@EnableKafka
class KafkaConfig {

    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, String>,
        meterRegistry: MeterRegistry
    ): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.containerProperties.setErrorHandler(KafkaErrorHandler(meterRegistry))
        factory.consumerFactory = consumerFactory
        return factory
    }

    @Bean
    @Profile(value = ["remote", "local-kafka"])
    @Primary
    fun consumerFactory(properties: KafkaProperties): ConsumerFactory<String, String> {
        return DefaultKafkaConsumerFactory(properties.buildConsumerProperties())
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, String>): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    @Profile(value = ["remote", "local-kafka"])
    @Primary
    fun producerFactory(properties: KafkaProperties): ProducerFactory<String, String> {
        return DefaultKafkaProducerFactory(properties.buildProducerProperties())
    }
}
