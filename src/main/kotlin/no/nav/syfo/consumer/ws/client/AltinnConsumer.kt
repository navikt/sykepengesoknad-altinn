package no.nav.syfo.consumer.ws.client

import no.altinn.schemas.services.intermediary.receipt._2009._10.ReceiptStatusEnum
import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternal
import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalInsertCorrespondenceV2AltinnFaultFaultFaultMessage
import no.nav.syfo.SoknadAltinnMapper
import no.nav.syfo.SykepengesoknadAltinn
import no.nav.syfo.log
import org.springframework.stereotype.Component
import javax.inject.Inject


@Component
class AltinnConsumer @Inject
constructor(private val insertCorrespondenceV2: ICorrespondenceAgencyExternal,
            private val soknadAltinnMapper: SoknadAltinnMapper) {
    val log = log()

    val SYSTEM_USER_CODE = "NAV_DIGISYFO"

    fun sendSykepengesoknadTilArbeidsgiver(sykepengesoknadAltinn: SykepengesoknadAltinn): Int? {
        try {
            val receiptExternal = insertCorrespondenceV2.insertCorrespondenceV2(
                    SYSTEM_USER_CODE, sykepengesoknadAltinn.sykepengesoknad.id,
                    soknadAltinnMapper.sykepengesoeknadTilCorrespondence(sykepengesoknadAltinn)
            )
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