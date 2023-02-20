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
import no.nav.syfo.domain.AltinnInnsendelseEkstraData
import no.nav.syfo.domain.soknad.Avsendertype.SYSTEM
import no.nav.syfo.domain.soknad.Soknadstype
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.toggles.EnvironmentToggles
import org.springframework.stereotype.Component
import java.io.IOException
import java.time.format.DateTimeFormatter
import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName

@Component
class SoknadAltinnMapper(private val toggle: EnvironmentToggles) {

    val log = logger()

    private val SYKEPENGESOEKNAD_TJENESTEKODE = "4751" // OBS! VIKTIG! Denne må ikke endres, da kan feil personer få tilgang til sykepengesøknaden i Altinn!
    private val SYKEPENGESOEKNAD_TJENESTEVERSJON = "1"

    fun sykepengesoeknadTilCorrespondence(sykepengesoknad: Sykepengesoknad, ekstraData: AltinnInnsendelseEkstraData): InsertCorrespondenceV2 {
        val namespace = "http://schemas.altinn.no/services/ServiceEngine/Correspondence/2010/10"
        val binaryNamespace = "http://www.altinn.no/services/ServiceEngine/ReporteeElementList/2010/10"

        val tittel = opprettTittel(sykepengesoknad, ekstraData)
        val sykepengesoeknadTekst = opprettInnholdstekst(sykepengesoknad)

        return InsertCorrespondenceV2()
            .withAllowForwarding(JAXBElement(QName(namespace, "AllowForwarding"), Boolean::class.java, false))
            .withReportee(
                JAXBElement(
                    QName(namespace, "Reportee"), String::class.java,
                    getOrgnummerForSendingTilAltinn(sykepengesoknad.arbeidsgiver.orgnummer)
                )
            )
            .withMessageSender(
                JAXBElement(
                    QName(namespace, "MessageSender"), String::class.java,
                    byggMessageSender(sykepengesoknad, ekstraData)
                )
            )
            .withServiceCode(JAXBElement(QName(namespace, "ServiceCode"), String::class.java, SYKEPENGESOEKNAD_TJENESTEKODE))
            .withServiceEdition(JAXBElement(QName(namespace, "ServiceEdition"), String::class.java, SYKEPENGESOEKNAD_TJENESTEVERSJON))
            .withNotifications(opprettNotifications(namespace))
            .withContent(tilInnhold(namespace, binaryNamespace, tittel, sykepengesoeknadTekst, ekstraData))
    }

    private fun opprettTittel(sykepengesoknad: Sykepengesoknad, ekstraData: AltinnInnsendelseEkstraData): String {
        return when (sykepengesoknad.type) {
            Soknadstype.BEHANDLINGSDAGER -> tittelTekst("Søknad med enkeltstående behandlingsdager", sykepengesoknad, ekstraData)
            Soknadstype.GRADERT_REISETILSKUDD -> tittelTekst("Søknad om Gradert reisetilskudd", sykepengesoknad, ekstraData)
            else -> tittelTekst("Søknad om sykepenger", sykepengesoknad, ekstraData)
        }
    }

    private fun tittelTekst(type: String, sykepengesoknad: Sykepengesoknad, ekstraData: AltinnInnsendelseEkstraData): String {
        return "$type - ${periodeSomTekst(sykepengesoknad)} - ${ekstraData.navn} (${ekstraData.fnr})${if (sykepengesoknad.sendtNav != null) " - sendt til NAV" else ""}"
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

    private fun byggMessageSender(sykepengesoknad: Sykepengesoknad, ekstraData: AltinnInnsendelseEkstraData): String {
        if (sykepengesoknad.avsendertype == SYSTEM) {
            return "Autogenerert på grunn av et registrert dødsfall"
        }

        return ekstraData.navn + " - " + ekstraData.fnr
    }

    private fun opprettNotifications(namespace: String): JAXBElement<NotificationBEList> {
        return JAXBElement(
            QName(namespace, "Notifications"), NotificationBEList::class.java,
            NotificationBEList()
                .withNotification(epostNotification(), smsNotification())
        )
    }

    private fun epostNotification(): Notification {
        return opprettEpostNotification(
            listOf(
                "Ny søknad om sykepenger i Altinn",
                "<p>En ansatt i \$reporteeName$ (\$reporteeNumber$) har sendt inn en søknad om sykepenger.</p>" +
                    "<p>" +
                    "Logg inn på Altinn for å se søknaden.</p>" +
                    "<p>Vennlig hilsen NAV.</p>"
            )
        )
    }

    private fun smsNotification(): Notification {
        return opprettSMSNotification(
            listOf(
                "En ansatt i \$reporteeName$ (\$reporteeNumber$) har sendt inn en søknad om sykepenger. ",
                "Logg inn på Altinn for å se søknaden. Vennlig hilsen NAV."
            )
        )
    }

    private fun tilInnhold(namespace: String, binaryNamespace: String, tittel: String, sykepengesoeknadTekst: String, ekstraData: AltinnInnsendelseEkstraData): JAXBElement<ExternalContentV2> {
        return JAXBElement(
            QName(namespace, "Content"), ExternalContentV2::class.java,
            ExternalContentV2()
                .withLanguageCode(JAXBElement(QName(namespace, "LanguageCode"), String::class.java, "1044"))
                .withMessageTitle(JAXBElement(QName(namespace, "MessageTitle"), String::class.java, tittel))
                .withMessageBody(JAXBElement(QName(namespace, "MessageBody"), String::class.java, sykepengesoeknadTekst))
                .withCustomMessageData(null)
                .withAttachments(
                    JAXBElement(
                        QName(namespace, "Attachments"), AttachmentsV2::class.java,
                        AttachmentsV2()
                            .withBinaryAttachments(
                                JAXBElement(
                                    QName(namespace, "BinaryAttachments"), BinaryAttachmentExternalBEV2List::class.java,
                                    BinaryAttachmentExternalBEV2List()
                                        .withBinaryAttachmentV2(
                                            pdfVedlegg(binaryNamespace, ekstraData.pdf),
                                            xmlVedlegg(binaryNamespace, ekstraData.xml)
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
        return opprettBinaertVedlegg(binaryNamespace, xml, SHOW_TO_ALL, "Sykepengesøknad maskinlesbar", "sykepengesoeknad.xml")
    }

    private fun opprettBinaertVedlegg(binaryNamespace: String, bytes: ByteArray, restriction: UserTypeRestriction, name: String, fileName: String): BinaryAttachmentV2 {
        return BinaryAttachmentV2()
            .withDestinationType(restriction)
            .withFileName(JAXBElement(QName(binaryNamespace, "FileName"), String::class.java, fileName))
            .withName(JAXBElement(QName(binaryNamespace, "Name"), String::class.java, name))
            .withFunctionType(AttachmentFunctionType.fromValue("Unspecified"))
            .withEncrypted(false)
            .withSendersReference(JAXBElement(QName(binaryNamespace, "SendersReference"), String::class.java, "senders ref"))
            .withData(JAXBElement(QName("http://www.altinn.no/services/ServiceEngine/ReporteeElementList/2010/10", "Data"), ByteArray::class.java, bytes))
    }

    fun getOrgnummerForSendingTilAltinn(orgnummer: String) =
        if (toggle.isProd() || toggle.allowsOrgnummer(orgnummer)) {
            orgnummer
        } else {
            "910067494" // dette er default orgnummer i test: 'GODVIK OG FLATÅSEN'
        }

    fun periodeSomTekst(sykepengesoknad: Sykepengesoknad): String {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        return dateTimeFormatter.format(sykepengesoknad.fom) + "-" + dateTimeFormatter.format(sykepengesoknad.tom)
    }
}
