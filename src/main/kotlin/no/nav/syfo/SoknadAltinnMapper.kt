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
constructor(private val pdfRestController: PDFRestController) {

    val log = log()

    private val SYKEPENGESOEKNAD_TJENESTEKODE = "4751" // OBS! VIKTIG! Denne må ikke endres, da kan feil personer få tilgang til sykepengesøknaden i Altinn!
    private val SYKEPENGESOEKNAD_TJENESTEVERSJON = "1"

    fun sykepengesoeknadTilCorrespondence(sykepengesoknadAltinn: SykepengesoknadAltinn): InsertCorrespondenceV2 {
        val namespace = "http://schemas.altinn.no/services/ServiceEngine/Correspondence/2010/10"
        val binaryNamespace = "http://www.altinn.no/services/ServiceEngine/ReporteeElementList/2010/10"
        val replyOptionNamespace = "http://schemas.altinn.no/services/ServiceEngine/Correspondence/2009/10"

        val tittel = opprettTittel(sykepengesoknadAltinn)
        val sykepengesoeknadTekst = opprettInnholdstekst(sykepengesoknadAltinn.sykepengesoknad)

        return InsertCorrespondenceV2()
                .withAllowForwarding(JAXBElement<Boolean>(QName(namespace, "AllowForwarding"), Boolean::class.java, false))
                .withReportee(JAXBElement<String>(QName(namespace, "Reportee"), String::class.java,
                        sykepengesoknadAltinn.sykepengesoknad.orgnummerArbeidsgiver))
                .withMessageSender(JAXBElement<String>(QName(namespace, "MessageSender"), String::class.java,
                        byggMessageSender(sykepengesoknadAltinn)))
                .withServiceCode(JAXBElement<String>(QName(namespace, "ServiceCode"), String::class.java, SYKEPENGESOEKNAD_TJENESTEKODE))
                .withServiceEdition(JAXBElement<String>(QName(namespace, "ServiceEdition"), String::class.java, SYKEPENGESOEKNAD_TJENESTEVERSJON))
                .withNotifications(opprettNotifications(namespace))
                .withContent(tilInnhold(sykepengesoknadAltinn, namespace, binaryNamespace, tittel, sykepengesoeknadTekst))
    }

    private fun opprettTittel(sykepengesoknadAltinn: SykepengesoknadAltinn): String {
        val sykepengesoknad = sykepengesoknadAltinn.sykepengesoknad

        val brukersNavn = sykepengesoknad.navn
        val fnr = sykepengesoknad.fnr

        //TODO er søknaden sendt til nav?
        //return if (sykepengesoknad.sendtTilNAVDato != null) {
        return if(true) {
            "Søknad om sykepenger - " + periodeSomTekst(sykepengesoknad) + " - " + brukersNavn + " (" + fnr + ") - sendt til NAV"
        } else {
            "Søknad om sykepenger - " + periodeSomTekst(sykepengesoknad) + " - " + brukersNavn + " (" + fnr + ")"
        }
    }

    private fun opprettInnholdstekst(sykepengesoknad: Sykepengesoknad): String {
        try {
            //TODO sjekk om soknad er sendt til både nav og arbeidsgiver
            //return if (sykepengesoknad.sendtTilNAVDato != null && sykepengesoknad.sendtTilArbeidsgiverDato != null) {
            return if (true) {
                SykepengesoknadAltinn::class.java.getResource("/sykepengesoknad-sendt-til-AG-og-NAV-tekst.html").readText()

            } else {
                SykepengesoknadAltinn::class.java.getResource("/sykepengesoknad-sendt-til-AG-tekst.html").readText()
            }
        } catch (e: IOException) {
            log.error("Feil med henting av sykepengesoknad-sendt-til-AG-tekst.html", e)
        }

        return ""
    }

    private fun byggMessageSender(sykepengesoknadAltinn: SykepengesoknadAltinn): String {
        //TODO er søknaden autogenerert?
        //if (sykepengesoknadAltinn.sykepengesoknad.isAutogenerertBrukerDoed()) {
        if (false) {
            return "Autogenerert på grunn av et registrert dødsfall"
        }

        val brukersNavn = sykepengesoknadAltinn.sykepengesoknad.navn
        val fnr = sykepengesoknadAltinn.sykepengesoknad.fnr

        return brukersNavn + " - " + fnr
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

    private fun tilInnhold(sykepengesoknadAltinn: SykepengesoknadAltinn, namespace: String, binaryNamespace: String, tittel: String, sykepengesoeknadTekst: String): JAXBElement<ExternalContentV2> {
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
                                                                pdfVedlegg(binaryNamespace, sykepengesoknadAltinn),
                                                                xmlVedlegg(binaryNamespace, sykepengesoknadAltinn)
                                                        )
                                        )
                                )
                        )
                )
        )
    }

    private fun pdfVedlegg(binaryNamespace: String, sykepengesoknadAltinn: SykepengesoknadAltinn): BinaryAttachmentV2 {
        return opprettBinaertVedlegg(binaryNamespace, pdfRestController.getPDF(sykepengesoknadAltinn.sykepengesoknad, PDFTemplate.ARBEIDSTAKERE), SHOW_TO_ALL, "Sykepengesøknad", "Sykepengesøknad.pdf")
    }

    //TODO få med XML
    private fun xmlVedlegg(binaryNamespace: String, sykepengesoknadAltinn: SykepengesoknadAltinn): BinaryAttachmentV2 {
        return opprettBinaertVedlegg(binaryNamespace, "DETTE ER EN XML".toByteArray(), SHOW_TO_ALL, "Sykepengesøknad maskinlesbar", "sykepengesoknad.xml")
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

    //TODO trengs denne?
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
