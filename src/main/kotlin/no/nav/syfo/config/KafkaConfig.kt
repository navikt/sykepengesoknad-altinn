package no.nav.syfo.config

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.kafka.KafkaErrorHandler
import no.nav.syfo.kafka.LegacyMultiFunctionDeserializer
import no.nav.syfo.kafka.MELDINGSTYPE
import no.nav.syfo.kafka.getLastHeaderByKeyAsString
import no.nav.syfo.kafka.interfaces.Soknad
import no.nav.syfo.kafka.soknad.dto.SoknadDTO
import no.nav.syfo.kafka.soknad.serializer.FunctionSerializer
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import no.nav.syfo.kafka.sykepengesoknadbehandlingsdager.dto.SykepengesoknadBehandlingsdagerDTO
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE

private val objectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerModule(KotlinModule())
        .configure(READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)

@Configuration
@EnableKafka
class KafkaConfig(private val properties: KafkaProperties) {

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Sykepengesoknad> = KafkaTemplate(
            DefaultKafkaProducerFactory(
                    properties.buildProducerProperties(),
                    StringSerializer(),
                    FunctionSerializer<Sykepengesoknad>(objectMapper::writeValueAsBytes)
            )
    )

    object SykepengesoknadBehandlingsdagerDTODeserializer : Deserializer<SykepengesoknadBehandlingsdagerDTO> {
        override fun close() {
        }

        override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
        }

        override fun deserialize(topic: String?, data: ByteArray?): SykepengesoknadBehandlingsdagerDTO {
            return objectMapper.readValue(data, SykepengesoknadBehandlingsdagerDTO::class.java)
        }

    }

    object SykepengesoknadDeserializer : Deserializer<Sykepengesoknad> {
        override fun close() {
        }

        override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
        }

        override fun deserialize(topic: String?, data: ByteArray?): Sykepengesoknad {
            return objectMapper.readValue(data, Sykepengesoknad::class.java)
        }

    }

    @Bean
    fun kafkaListenerContainerFactory(
            consumerFactory: ConsumerFactory<String, Soknad>,
            kafkaErrorHandler: KafkaErrorHandler
    ): ConcurrentKafkaListenerContainerFactory<String, Soknad> =
            ConcurrentKafkaListenerContainerFactory<String, Soknad>()
                    .apply {
                        containerProperties.ackMode = MANUAL_IMMEDIATE
                        containerProperties.setErrorHandler(kafkaErrorHandler)
                        setRecordFilterStrategy { consumerRecord ->
                            "SYKEPENGESOKNAD" != getLastHeaderByKeyAsString(consumerRecord.headers(), MELDINGSTYPE)
                        }
                        this.consumerFactory = consumerFactory
                    }

    @Bean
    @Profile(value = ["remote", "local-kafka"])
    @Primary
    fun consumerFactory(properties: KafkaProperties): ConsumerFactory<String, Soknad> {

        return DefaultKafkaConsumerFactory(
                properties.buildConsumerProperties(),
                StringDeserializer(),
                LegacyMultiFunctionDeserializer<Soknad>(
                        deserializeMap = mapOf(
                                "SYKEPENGESOKNAD" to { _, bytes -> bytes?.let { objectMapper.readValue(it, SykepengesoknadDTO::class.java) } as Soknad },
                                "SOKNAD" to { _, bytes -> bytes?.let { objectMapper.readValue(it, SoknadDTO::class.java) } as Soknad }
                        ),
                        deserializeDefault = { bytes -> bytes?.let { objectMapper.readValue(it, SykepengesoknadDTO::class.java) }!! })
        )
    }

    @Bean
    fun kafkaListenerContainerFactoryBehandlingsdager(
            consumerFactory: ConsumerFactory<String, SykepengesoknadBehandlingsdagerDTO>,
            kafkaErrorHandler: KafkaErrorHandler
    ): ConcurrentKafkaListenerContainerFactory<String, SykepengesoknadBehandlingsdagerDTO> =
            ConcurrentKafkaListenerContainerFactory<String, SykepengesoknadBehandlingsdagerDTO>()
                    .apply {
                        containerProperties.ackMode = MANUAL_IMMEDIATE
                        containerProperties.setErrorHandler(kafkaErrorHandler)
                        this.consumerFactory = consumerFactory
                    }

    @Bean
    @Profile(value = ["remote", "local-kafka"])
    fun consumerFactoryBehandlingsdager(
            properties: KafkaProperties): ConsumerFactory<String, SykepengesoknadBehandlingsdagerDTO> {

        return DefaultKafkaConsumerFactory(
                properties.buildConsumerProperties(),
                StringDeserializer(),
                SykepengesoknadBehandlingsdagerDTODeserializer
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
    @Profile(value = ["remote", "local-kafka"])
    fun consumerFactoryRebehandling(
            properties: KafkaProperties): ConsumerFactory<String, Sykepengesoknad> {

        return DefaultKafkaConsumerFactory(
                properties.buildConsumerProperties(),
                StringDeserializer(),
                SykepengesoknadDeserializer
        )
    }
}
