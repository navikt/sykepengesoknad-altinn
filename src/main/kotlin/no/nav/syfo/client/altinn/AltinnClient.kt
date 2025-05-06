package no.nav.syfo.client.altinn

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import no.altinn.schemas.services.intermediary.receipt._2009._10.ReceiptStatusEnum
import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasic
import no.nav.syfo.SoknadAltinnMapper
import no.nav.syfo.domain.AltinnInnsendelseEkstraData
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.zip.GZIPOutputStream

private const val SYSTEM_USER_CODE = "NAV_DIGISYFO"

@Component
class AltinnClient(
    private val iCorrespondenceAgencyExternalBasic: ICorrespondenceAgencyExternalBasic,
    private val soknadAltinnMapper: SoknadAltinnMapper,
    @Value("\${altinn.username}") private val altinnUsername: String,
    @Value("\${altinn.password}") private val altinnPassword: String,
    private val storage: Storage,
    @Value("\${BUCKET_NAME}") val bucketName: String,
    @Value("\${lagre.alle.dokumenter}") private val lagreAlleDokumenter: Boolean,
) {
    private val log = logger()

    fun sendSykepengesoknadTilArbeidsgiver(
        sykepengesoknad: Sykepengesoknad,
        ekstraData: AltinnInnsendelseEkstraData,
    ): Int {
        val mappe = "${sykepengesoknad.id}/${mappeTidspunkt()}/"

        fun lagreFil(
            filnavn: String,
            contentType: String,
            content: ByteArray,
        ) {
            val blobInfo =
                BlobInfo
                    .newBuilder(
                        BlobId.of(bucketName, mappe + filnavn),
                    ).setContentType(contentType)
                    .build()
            storage.create(blobInfo, content)
        }
        try {
            val correspondence =
                soknadAltinnMapper.sykepengesoeknadTilCorrespondence(sykepengesoknad, ekstraData)

            val serialisertRequest = correspondence.serialiser()
            if (lagreAlleDokumenter) {
                lagreFil(
                    filnavn = "sykepengesoknad.pdf",
                    contentType = "application/pdf",
                    content = ekstraData.pdf,
                )
                lagreFil(
                    filnavn = "sykepengesoknad.xml",
                    contentType = "application/xml",
                    content = ekstraData.xml,
                )
                lagreFil(
                    filnavn = "correspondence.xml",
                    contentType = "application/xml",
                    content = serialisertRequest.toByteArray(),
                )
            }
            lagreFil(
                filnavn = "correspondence.gz",
                contentType = "application/gzip",
                content = serialisertRequest.gzip(),
            )

            val receiptExternal =
                iCorrespondenceAgencyExternalBasic.insertCorrespondenceBasicV2(
                    altinnUsername,
                    altinnPassword,
                    SYSTEM_USER_CODE,
                    sykepengesoknad.id,
                    correspondence,
                )
            val serialisertReceipt = receiptExternal.serialiser()
            lagreFil(
                filnavn = "receiptExternal.gz",
                contentType = "application/gzip",
                content = serialisertReceipt.gzip(),
            )
            if (lagreAlleDokumenter) {
                lagreFil(
                    filnavn = "receiptExternal.xml",
                    contentType = "application/xml",
                    content = serialisertReceipt.toByteArray(),
                )
            }
            if (receiptExternal.receiptStatusCode != ReceiptStatusEnum.OK) {
                log.error("Fikk uventet statuskode fra Altinn {}", receiptExternal.receiptStatusCode)
                throw RuntimeException("feil")
            }
            return receiptExternal.receiptId
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun String.gzip(): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).bufferedWriter(UTF_8).use { it.write(this) }
        return bos.toByteArray()
    }

    private fun mappeTidspunkt() =
        Instant
            .now()
            .truncatedTo(ChronoUnit.SECONDS)
            .toString()
            .replace("-", "")
            .replace(":", "")
}
