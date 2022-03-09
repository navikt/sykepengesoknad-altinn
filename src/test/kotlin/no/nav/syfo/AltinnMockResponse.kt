package no.nav.syfo

import no.altinn.services.serviceengine.correspondence._2009._10.InsertCorrespondenceBasicV2
import no.nav.syfo.Testoppsett.Companion.altinnMockWebserver
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.io.StringReader
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamReader

fun mockAltinnResponse(recieptId: Int = (Math.random() * 10000).toInt()): Int {
    val response = """
    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
        <soap:Body>
            <InsertCorrespondenceBasicV2Response xmlns="http://www.altinn.no/services/ServiceEngine/Correspondence/2009/10" xmlns:res="http://schemas.altinn.no/services/Intermediary/Receipt/2009/10">
                <InsertCorrespondenceBasicV2Result>
                    <res:ReceiptId>$recieptId</res:ReceiptId>
                    <res:ReceiptStatusCode>OK</res:ReceiptStatusCode>
                </InsertCorrespondenceBasicV2Result>
            </InsertCorrespondenceBasicV2Response>
        </soap:Body>
    </soap:Envelope>"""

    altinnMockWebserver.enqueue(MockResponse().setBody(response))
    return recieptId
}

fun RecordedRequest.parseCorrespondence(): InsertCorrespondenceBasicV2 {
    val requestBody = this.body.readUtf8()
    val sr = XMLInputFactory.newFactory().createXMLStreamReader(StringReader(requestBody))
    while (sr.hasNext()) {
        val type = sr.next()
        if (type == XMLStreamReader.START_ELEMENT && "InsertCorrespondenceBasicV2" == sr.localName) {

            val jc: JAXBContext = JAXBContext.newInstance(InsertCorrespondenceBasicV2::class.java)
            val unmarshaller = jc.createUnmarshaller()
            val je: JAXBElement<InsertCorrespondenceBasicV2> = unmarshaller.unmarshal(sr, InsertCorrespondenceBasicV2::class.java)
            return je.value
        }
    }
    throw RuntimeException("Fant ikke forventa element")
}
