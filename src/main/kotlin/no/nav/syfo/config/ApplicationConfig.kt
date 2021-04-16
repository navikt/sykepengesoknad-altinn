package no.nav.syfo.config

import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.web.client.RestTemplate
import javax.sql.DataSource

@Configuration
class ApplicationConfig {

    @Bean
    fun restTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        restTemplate.messageConverters.stream()
                .filter { AbstractJackson2HttpMessageConverter::class.java.isInstance(it) }
                .map { AbstractJackson2HttpMessageConverter::class.java.cast(it) }
                .map { t: AbstractJackson2HttpMessageConverter? -> t?.objectMapper }
                .forEach { objectMapper -> objectMapper?.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false) }
        return restTemplate
    }

    @Bean
    fun basicAuthRestTemplate(@Value("\${srvsyfoaltinn.username}") username: String,
                              @Value("\${srvsyfoaltinn.password}") password: String): RestTemplate {
        return RestTemplateBuilder()
                .basicAuthentication(username, password)
                .build()
    }

    @Bean
    @Primary
    fun datasourceTransactionManager(dataSource: DataSource): DataSourceTransactionManager {
        val dataSourceTransactionManager = DataSourceTransactionManager()
        dataSourceTransactionManager.dataSource = dataSource
        return dataSourceTransactionManager
    }

    @Bean
    fun flywayMigrationStrategy(dataSourceTransactionManager: DataSourceTransactionManager): FlywayMigrationStrategy {
        return FlywayMigrationStrategy { it.migrate() }
    }
}
