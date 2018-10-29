package no.nav.syfo.config

import org.junit.ClassRule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.test.rule.KafkaEmbedded

import org.springframework.kafka.test.utils.KafkaTestUtils.consumerProps
import org.springframework.kafka.test.utils.KafkaTestUtils.producerProps

@Configuration
@EnableKafka
class KafkaTestConfig {

    companion object {
        @ClassRule
        var embeddedKafka = KafkaEmbedded(1, true, "test")
    }

    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        return DefaultKafkaProducerFactory(producerProps(embeddedKafka))
    }

    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        return DefaultKafkaConsumerFactory(consumerProps("test", "false", embeddedKafka))
    }
}
