package no.nav.syfo.bucket

import com.google.cloud.storage.Storage
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class TestBucketConfig {
    @Bean
    @Profile("test")
    fun bucket(): Storage {
        return LocalStorageHelper.getOptions().service
    }
}
