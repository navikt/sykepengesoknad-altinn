package no.nav.syfo.kafka

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties

@Configuration
class AivenKafkaConfig(
    @Value("\${KAFKA_BROKERS}") private val kafkaBrokers: String,
    @Value("\${KAFKA_TRUSTSTORE_PATH}") private val kafkaTruststorePath: String,
    @Value("\${aiven-kafka.auto-offset-reset}") private val kafkaAutoOffsetReset: String,
    @Value("\${aiven-kafka.security-protocol}") private val kafkaSecurityProtocol: String,
    @Value("\${KAFKA_CREDSTORE_PASSWORD}") private val kafkaCredstorePassword: String,
    @Value("\${KAFKA_KEYSTORE_PATH}") private val kafkaKeystorePath: String,
) {
    private val JAVA_KEYSTORE = "JKS"
    private val PKCS12 = "PKCS12"

    fun commonConfig() = mapOf(
        BOOTSTRAP_SERVERS_CONFIG to kafkaBrokers
    ) + securityConfig()

    private fun securityConfig() = mapOf(
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to kafkaSecurityProtocol,
        SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG to "", // Disable server host name verification
        SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG to JAVA_KEYSTORE,
        SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to PKCS12,
        SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to kafkaTruststorePath,
        SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to kafkaCredstorePassword,
        SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to kafkaKeystorePath,
        SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to kafkaCredstorePassword,
        SslConfigs.SSL_KEY_PASSWORD_CONFIG to kafkaCredstorePassword,
    )

    @Bean
    fun aivenKafkaListenerContainerFactory(
        kafkaErrorHandler: KafkaErrorHandler
    ): ConcurrentKafkaListenerContainerFactory<String, String> {
        val config = mapOf(
            ConsumerConfig.GROUP_ID_CONFIG to "syfoaltinn-consumer",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to kafkaAutoOffsetReset,
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.MAX_POLL_RECORDS_CONFIG to "1"
        ) + commonConfig()
        val consumerFactory = DefaultKafkaConsumerFactory<String, String>(config)

        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactory
        factory.setErrorHandler(kafkaErrorHandler)
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        return factory
    }

    fun skapProducer(): KafkaProducer<String, String> {
        val configs = producerConfig()
        return KafkaProducer<String, String>(configs)
    }

    private fun producerConfig() = mapOf(
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ProducerConfig.ACKS_CONFIG to "all",
        ProducerConfig.RETRIES_CONFIG to 10,
        ProducerConfig.RETRY_BACKOFF_MS_CONFIG to 100
    ) + commonConfig()
}
