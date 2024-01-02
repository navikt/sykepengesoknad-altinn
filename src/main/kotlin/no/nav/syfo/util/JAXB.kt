package no.nav.syfo.util

import no.nav.melding.virksomhet.sykepengesoeknadarbeidsgiver.v1.sykepengesoeknadarbeidsgiver.ObjectFactory
import org.springframework.core.io.ClassPathResource
import org.xml.sax.SAXException
import java.io.IOException
import java.io.StringWriter
import javax.xml.XMLConstants
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller
import javax.xml.bind.ValidationEventHandler
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory

object JAXB {
    private var sykepengesoeknadArbeidsgiverContext: JAXBContext

    init {
        try {
            sykepengesoeknadArbeidsgiverContext =
                JAXBContext.newInstance(
                    ObjectFactory::class.java,
                )
        } catch (jaxbException: JAXBException) {
            throw RuntimeException(jaxbException)
        }
    }

    fun marshallSykepengesoeknadArbeidsgiver(
        element: Any?,
        handler: ValidationEventHandler?,
    ): String {
        return try {
            val writer = StringWriter()
            val marshaller = sykepengesoeknadArbeidsgiverContext.createMarshaller()
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false)
            marshaller.schema = sykmeldingArbeidsgiverSchema()
            if (handler != null) marshaller.eventHandler = handler
            marshaller.marshal(element, StreamResult(writer))
            writer.toString()
        } catch (e: JAXBException) {
            throw RuntimeException("Feil ved marshall av sykepengesøknaden", e)
        } catch (e: SAXException) {
            throw RuntimeException("Feil ved marshall av sykepengesøknaden", e)
        } catch (e: IOException) {
            throw RuntimeException("Feil ved marshall av sykepengesøknaden", e)
        }
    }

    @Throws(IOException::class, SAXException::class)
    private fun sykmeldingArbeidsgiverSchema(): Schema {
        val resource = ClassPathResource("/sykepengesoeknadarbeidsgiver.xsd")
        val inputStream = resource.inputStream
        val sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        return sf.newSchema(StreamSource(inputStream))
    }
}
