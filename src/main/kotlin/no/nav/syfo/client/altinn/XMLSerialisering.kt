package no.nav.syfo.client.altinn

import no.altinn.schemas.services.intermediary.receipt._2009._10.ReceiptExternal
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.InsertCorrespondenceV2
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.ObjectFactory
import java.io.StringWriter
import javax.xml.bind.JAXBContext

fun InsertCorrespondenceV2.serialiser(): String {
    val writer = StringWriter()
    val context: JAXBContext = JAXBContext.newInstance(InsertCorrespondenceV2::class.java)
    val objectFactory = ObjectFactory()
    val m = context.createMarshaller()
    m.marshal(objectFactory.createInsertCorrespondenceV2(this), writer)
    return writer.toString()
}

fun ReceiptExternal.serialiser(): String {
    val writer = StringWriter()
    val context: JAXBContext = JAXBContext.newInstance(ReceiptExternal::class.java)
    val objectFactory =
        no.altinn.schemas.services.intermediary.receipt._2009._10
            .ObjectFactory()
    val m = context.createMarshaller()
    m.marshal(objectFactory.createReceiptExternal(this), writer)
    return writer.toString()
}
