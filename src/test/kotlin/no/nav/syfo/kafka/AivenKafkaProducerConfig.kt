package no.nav.syfo.kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AivenKafkaProducerConfig(
    private val aivenKafkaConfig: AivenKafkaConfig,
) {
    @Bean
    fun aivenKafkaProducer(): KafkaProducer<String, String> {
        return KafkaProducer<String, String>(producerConfig())
    }

    private fun producerConfig() =
        mapOf(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.RETRIES_CONFIG to 10,
            ProducerConfig.RETRY_BACKOFF_MS_CONFIG to 100,
        ) + aivenKafkaConfig.commonConfig()
}
