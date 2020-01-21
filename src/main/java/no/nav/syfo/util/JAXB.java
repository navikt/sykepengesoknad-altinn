package no.nav.syfo.util;

import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import static java.lang.Boolean.FALSE;
import static javax.xml.bind.JAXBContext.newInstance;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

public class JAXB {

    private static final JAXBContext SYKEPENGESOEKNAD_ARBEIDSGIVER_CONTEXT;

    static {
        try {
            SYKEPENGESOEKNAD_ARBEIDSGIVER_CONTEXT = newInstance(
                    no.nav.melding.virksomhet.sykepengesoeknadarbeidsgiver.v1.sykepengesoeknadarbeidsgiver.ObjectFactory.class
            );
        } catch (JAXBException jaxbException) {
            throw new RuntimeException(jaxbException);
        }
    }

    public static String marshallSykepengesoeknadArbeidsgiver(Object element, ValidationEventHandler handler) {
        try {
            StringWriter writer = new StringWriter();
            Marshaller marshaller = SYKEPENGESOEKNAD_ARBEIDSGIVER_CONTEXT.createMarshaller();
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, FALSE);
            marshaller.setSchema(sykmeldingArbeidsgiverSchema());
            if (handler != null) marshaller.setEventHandler(handler);
            marshaller.marshal(element, new StreamResult(writer));
            return writer.toString();
        } catch (JAXBException | SAXException | IOException e) {
            throw new RuntimeException("Feil ved marshall av sykepengesøknaden", e);
        }
    }

    private static Schema sykmeldingArbeidsgiverSchema() throws IOException, SAXException {
        ClassPathResource resource = new ClassPathResource("/sykepengesoeknadarbeidsgiver.xsd");
        InputStream inputStream = resource.getInputStream();
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        return sf.newSchema(new StreamSource(inputStream));
    }
}
