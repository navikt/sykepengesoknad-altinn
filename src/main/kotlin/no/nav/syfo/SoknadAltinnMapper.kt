package no.nav.syfo

import no.altinn.schemas.services.serviceengine.correspondence._2010._10.AttachmentsV2
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.ExternalContentV2
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.InsertCorrespondenceV2
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.UserTypeRestriction
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.UserTypeRestriction.SHOW_TO_ALL
import no.altinn.schemas.services.serviceengine.notification._2009._10.Notification
import no.altinn.schemas.services.serviceengine.notification._2009._10.NotificationBEList
import no.altinn.schemas.services.serviceengine.subscription._2009._10.AttachmentFunctionType
import no.altinn.services.serviceengine.reporteeelementlist._2010._10.BinaryAttachmentExternalBEV2List
import no.altinn.services.serviceengine.reporteeelementlist._2010._10.BinaryAttachmentV2
import no.nav.syfo.domain.soknad.Sykepengesoknad
import org.springframework.stereotype.Component
import java.io.IOException
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName

@Component
class SoknadAltinnMapper @Inject
constructor() {

    val log = log()

    private val SYKEPENGESOEKNAD_TJENESTEKODE = "4751" // OBS! VIKTIG! Denne må ikke endres, da kan feil personer få tilgang til sykepengesøknaden i Altinn!
    private val SYKEPENGESOEKNAD_TJENESTEVERSJON = "1"

    fun sykepengesoeknadTilCorrespondence(sykepengesoknad: Sykepengesoknad): InsertCorrespondenceV2 {
        val namespace = "http://schemas.altinn.no/services/ServiceEngine/Correspondence/2010/10"
        val binaryNamespace = "http://www.altinn.no/services/ServiceEngine/ReporteeElementList/2010/10"

        val tittel = opprettTittel(sykepengesoknad)
        val sykepengesoeknadTekst = opprettInnholdstekst(sykepengesoknad)

        return InsertCorrespondenceV2()
                .withAllowForwarding(JAXBElement<Boolean>(QName(namespace, "AllowForwarding"), Boolean::class.java, false))
                .withReportee(JAXBElement<String>(QName(namespace, "Reportee"), String::class.java,
                        sykepengesoknad.arbeidsgiver.orgnummer))
                .withMessageSender(JAXBElement<String>(QName(namespace, "MessageSender"), String::class.java,
                        byggMessageSender(sykepengesoknad)))
                .withServiceCode(JAXBElement<String>(QName(namespace, "ServiceCode"), String::class.java, SYKEPENGESOEKNAD_TJENESTEKODE))
                .withServiceEdition(JAXBElement<String>(QName(namespace, "ServiceEdition"), String::class.java, SYKEPENGESOEKNAD_TJENESTEVERSJON))
                .withNotifications(opprettNotifications(namespace))
                .withContent(tilInnhold(sykepengesoknad, namespace, binaryNamespace, tittel, sykepengesoeknadTekst))
    }

    private fun opprettTittel(sykepengesoknad: Sykepengesoknad): String {
        return "Søknad om sykepenger - ${periodeSomTekst(sykepengesoknad)} - ${sykepengesoknad.navn} (${sykepengesoknad.fnr})${if (sykepengesoknad.sendtNav != null) " - sendt til NAV" else ""}"
    }

    private fun opprettInnholdstekst(sykepengesoknad: Sykepengesoknad): String {
        try {
            return if (sykepengesoknad.sendtNav != null && sykepengesoknad.sendtArbeidsgiver != null) {
                SoknadAltinnMapper::class.java.getResource("/sykepengesoknad-sendt-til-AG-og-NAV-tekst.html").readText()
            } else {
                SoknadAltinnMapper::class.java.getResource("/sykepengesoknad-sendt-til-AG-tekst.html").readText()
            }
        } catch (e: IOException) {
            log.error("Feil med henting av sykepengesoknad-sendt-til-AG-tekst.html", e)
        }

        return ""
    }

    private fun byggMessageSender(sykepengesoknad: Sykepengesoknad): String {
        //TODO er søknaden autogenerert?
        //if (sykepengesoknad.sykepengesoknad.isAutogenerertBrukerDoed()) {
        if (false) {
            return "Autogenerert på grunn av et registrert dødsfall"
        }

        return sykepengesoknad.navn + " - " + sykepengesoknad.fnr
    }

    private fun opprettNotifications(namespace: String): JAXBElement<NotificationBEList> {
        return JAXBElement<NotificationBEList>(QName(namespace, "Notifications"), NotificationBEList::class.java, NotificationBEList()
                .withNotification(epostNotification(), smsNotification()))
    }

    private fun epostNotification(): Notification {
        return opprettEpostNotification(listOf("Ny søknad om sykepenger i Altinn",
                "<p>En ansatt i \$reporteeName$ (\$reporteeNumber$) har sendt inn en søknad om sykepenger.</p>" +
                        "<p><a href=\"" + lenkeAltinnPortal() + "\">" +
                        "Logg inn på Altinn</a> for å se søknaden.</p>" +
                        "<p>Vennlig hilsen NAV.</p>"))
    }

    private fun smsNotification(): Notification {
        return opprettSMSNotification(listOf(
                "En ansatt i \$reporteeName$ (\$reporteeNumber$) har sendt inn en søknad om sykepenger. ",
                "Gå til " + smsLenkeAltinnPortal() + " for å se søknaden. Vennlig hilsen NAV."))
    }

    private fun tilInnhold(sykepengesoknad: Sykepengesoknad, namespace: String, binaryNamespace: String, tittel: String, sykepengesoeknadTekst: String): JAXBElement<ExternalContentV2> {
        return JAXBElement<ExternalContentV2>(QName(namespace, "Content"), ExternalContentV2::class.java, ExternalContentV2()
                .withLanguageCode(JAXBElement<String>(QName(namespace, "LanguageCode"), String::class.java, "1044"))
                .withMessageTitle(JAXBElement<String>(QName(namespace, "MessageTitle"), String::class.java, tittel))
                .withMessageBody(JAXBElement<String>(QName(namespace, "MessageBody"), String::class.java, sykepengesoeknadTekst))
                .withCustomMessageData(null)
                .withAttachments(
                        JAXBElement<AttachmentsV2>(QName(namespace, "Attachments"), AttachmentsV2::class.java, AttachmentsV2()
                                .withBinaryAttachments(
                                        JAXBElement<BinaryAttachmentExternalBEV2List>(QName(namespace, "BinaryAttachments"), BinaryAttachmentExternalBEV2List::class.java,
                                                BinaryAttachmentExternalBEV2List()
                                                        .withBinaryAttachmentV2(
                                                                pdfVedlegg(binaryNamespace, sykepengesoknad.pdf),
                                                                xmlVedlegg(binaryNamespace, sykepengesoknad.xml)
                                                        )
                                        )
                                )
                        )
                )
        )
    }

    private fun pdfVedlegg(binaryNamespace: String, pdf: ByteArray): BinaryAttachmentV2 {
        return opprettBinaertVedlegg(binaryNamespace, pdf, SHOW_TO_ALL, "Sykepengesøknad", "Sykepengesøknad.pdf")
    }

    private fun xmlVedlegg(binaryNamespace: String, xml: ByteArray): BinaryAttachmentV2 {
        return opprettBinaertVedlegg(binaryNamespace, xml, SHOW_TO_ALL, "Sykepengesøknad maskinlesbar", "sykepengesoknad.xml")
    }

    private fun opprettBinaertVedlegg(binaryNamespace: String, bytes: ByteArray, restriction: UserTypeRestriction, name: String, fileName: String): BinaryAttachmentV2 {
        return BinaryAttachmentV2()
                .withDestinationType(restriction)
                .withFileName(JAXBElement<String>(QName(binaryNamespace, "FileName"), String::class.java, fileName))
                .withName(JAXBElement<String>(QName(binaryNamespace, "Name"), String::class.java, name))
                .withFunctionType(AttachmentFunctionType.fromValue("Unspecified"))
                .withEncrypted(false)
                .withSendersReference(JAXBElement<String>(QName(binaryNamespace, "SendersReference"), String::class.java, "senders ref"))
                .withData(JAXBElement<ByteArray>(QName("http://www.altinn.no/services/ServiceEngine/ReporteeElementList/2010/10", "Data"), ByteArray::class.java, bytes))
    }

    //TODO trengs denne? JA, dette skal med - bør gjøres med unleash
    /*fun getOrgnummerForSendingTilAltinn(orgnummer: String): String {
        return ofNullable(getProperty("altinn.test.whitelist.orgnr"))
                .map({ whitelist -> asList(whitelist.split(",")) })
                .map({ whitelist -> whitelist.contains(orgnummer) })
                .filter(Predicate<Any> { it.booleanValue() })
                .map({ b -> orgnummer })
                .orElseGet({ getProperty("altinn.test.overstyr.orgnr", orgnummer) })
    }*/

    fun periodeSomTekst(sykepengesoknad: Sykepengesoknad): String {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        return dateTimeFormatter.format(sykepengesoknad.fom) + "-" + dateTimeFormatter.format(sykepengesoknad.tom)
    }


}
