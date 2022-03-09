package no.nav.syfo.storagetest

import com.google.cloud.storage.BucketInfo
import com.google.cloud.storage.StorageOptions
import no.nav.syfo.logger
import no.nav.syfo.toggles.EnvironmentToggles
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import javax.annotation.PostConstruct

@Component
class TestStorage(
    val env: EnvironmentToggles,
    @Value("\${BUCKET_NAME}") private val bucketName: String

) {
    val log = logger()

    @PostConstruct
    fun bucketTesting() {
        if (env.isDevGcp()) {
            val storage = StorageOptions.getDefaultInstance().service
            val bucket = storage.create(BucketInfo.of(bucketName))

            val blobName = Instant.now().toEpochMilli().toString() + "/" + "my_blob_name.txt"
            val blob = bucket.create(blobName, "Hello, World!".toByteArray(charset = Charsets.UTF_8), "text/plain")
            log.info("Oppretta blob " + blob.name)
        }
    }
}
