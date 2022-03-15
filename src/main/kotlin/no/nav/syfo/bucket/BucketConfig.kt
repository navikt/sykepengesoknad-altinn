package no.nav.syfo.bucket

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class BucketConfig {

    @Bean
    @Profile("default")
    fun bucket(): Storage {
        return StorageOptions.getDefaultInstance().service
    }
}
