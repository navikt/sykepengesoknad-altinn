package no.nav.syfo.consumer.ws.client

import no.altinn.schemas.services.intermediary.receipt._2009._10.ReceiptStatusEnum
import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasic
import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalInsertCorrespondenceV2AltinnFaultFaultFaultMessage
import no.nav.syfo.SoknadAltinnMapper
import no.nav.syfo.SykepengesoknadAltinn
import no.nav.syfo.log
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.inject.Inject


@Component
class AltinnConsumer @Inject
constructor(private val iCorrespondenceAgencyExternalBasic: ICorrespondenceAgencyExternalBasic,
            private val soknadAltinnMapper: SoknadAltinnMapper,
            @Value("\${altinnUser.username}") private val altinnUsername: String,
            @Value("\${altinnUser.password}") private val altinnPassword: String) {

    val log = log()

    val SYSTEM_USER_CODE = "NAV_DIGISYFO"

    fun sendSykepengesoknadTilArbeidsgiver(sykepengesoknadAltinn: SykepengesoknadAltinn): Int? {
        try {
            val receiptExternal = iCorrespondenceAgencyExternalBasic.insertCorrespondenceBasicV2(altinnUsername,
                    altinnPassword,
                    SYSTEM_USER_CODE,
                    sykepengesoknadAltinn.sykepengesoknad.id,
                    soknadAltinnMapper.sykepengesoeknadTilCorrespondence(sykepengesoknadAltinn))
            if (receiptExternal.receiptStatusCode != ReceiptStatusEnum.OK) {
                log.error("Fikk uventet statuskode fra Altinn {}", receiptExternal.receiptStatusCode)
                throw RuntimeException("feil")
            }
            return receiptExternal.receiptId
        } catch (e: ICorrespondenceAgencyExternalInsertCorrespondenceV2AltinnFaultFaultFaultMessage) {
            log.error("feil", e)
            throw RuntimeException("feil", e)
        } catch (e: Exception) {
            log.error("feil", e)
            throw e
        }
    }

}