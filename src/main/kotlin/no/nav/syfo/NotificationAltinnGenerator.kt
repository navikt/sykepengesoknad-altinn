package no.nav.syfo

import no.altinn.schemas.serviceengine.formsengine._2009._10.TransportType
import no.altinn.schemas.serviceengine.formsengine._2009._10.TransportType.EMAIL
import no.altinn.schemas.serviceengine.formsengine._2009._10.TransportType.SMS
import no.altinn.schemas.services.serviceengine.notification._2009._10.*
import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName

private val NORSK_BOKMAL = "1044"
private val FRA_EPOST_ALTINN = "noreply@altinn.no"
private val NOTIFICATION_NAMESPACE = "http://schemas.altinn.no/services/ServiceEngine/Notification/2009/10"

fun opprettEpostNotification(text: List<String>): Notification {
    return opprettNotification(FRA_EPOST_ALTINN, EMAIL, text)
}

fun opprettSMSNotification(text: List<String>): Notification {
    return opprettNotification(null, SMS, text)
}

private fun urlEncode(lenke: String): String {
    return lenke.replace("=".toRegex(), "%3D")
}

fun smsLenkeAltinnPortal(): String {
    return urlEncode(lenkeAltinnPortal())
}

fun lenkeAltinnPortal(): String {
    return "https://www.altinn.no/ui/MessageBox?O=\$reporteeNumber\$"
}

private fun opprettNotification(fraEpost: String?, type: TransportType, text: List<String>): Notification {
    return opprettNotification(fraEpost, type, konverterTilTextTokens(text))
}

private fun opprettNotification(fraEpost: String?, type: TransportType, textTokens: Array<TextToken>): Notification {
    if (textTokens.size != 2) {
        throw IllegalArgumentException("Antall textTokens må være 2. Var " + textTokens.size)
    }

    return Notification()
        .withLanguageCode(JAXBElement(QName(NOTIFICATION_NAMESPACE, "LanguageCode"), String::class.java, NORSK_BOKMAL))
        .withNotificationType(JAXBElement(QName(NOTIFICATION_NAMESPACE, "NotificationType"), String::class.java, "TokenTextOnly"))
        .withFromAddress(if (fraEpost == null) null else JAXBElement<String>(QName(NOTIFICATION_NAMESPACE, "FromAddress"), String::class.java, fraEpost))
        .withReceiverEndPoints(
            JAXBElement(
                QName(NOTIFICATION_NAMESPACE, "ReceiverEndPoints"), ReceiverEndPointBEList::class.java,
                ReceiverEndPointBEList()
                    .withReceiverEndPoint(
                        ReceiverEndPoint()
                            .withTransportType(JAXBElement(QName(NOTIFICATION_NAMESPACE, "TransportType"), TransportType::class.java, type))
                    )
            )
        )
        .withTextTokens(
            JAXBElement(
                QName(NOTIFICATION_NAMESPACE, "TextTokens"), TextTokenSubstitutionBEList::class.java,
                TextTokenSubstitutionBEList()
                    .withTextToken(*textTokens)
            )
        )
}

private fun konverterTilTextTokens(texts: List<String>): Array<TextToken> {
    return texts
        .mapIndexed { index, text ->
            TextToken().withTokenNum(index).withTokenValue(
                JAXBElement<String>(QName(NOTIFICATION_NAMESPACE, "TokenValue"), String::class.java, text)
            )
        }
        .toTypedArray()
}
