package no.nav.syfo

import no.nav.melding.virksomhet.sykepengesoeknadarbeidsgiver.v1.sykepengesoeknadarbeidsgiver.XMLSykepengesoeknadArbeidsgiver
import java.io.StringReader
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement
import javax.xml.bind.Unmarshaller

fun ByteArray.tilXMLSykepengesoeknadArbeidsgiver(): XMLSykepengesoeknadArbeidsgiver {
    val jaxbContext =
        JAXBContext.newInstance(no.nav.melding.virksomhet.sykepengesoeknadarbeidsgiver.v1.sykepengesoeknadarbeidsgiver.ObjectFactory::class.java)

    val jaxbUnmarshaller: Unmarshaller = jaxbContext.createUnmarshaller()
    val tekst = String(this)
    val reader = StringReader(tekst)

    @Suppress("UNCHECKED_CAST")
    val jaxbElement = jaxbUnmarshaller.unmarshal(reader) as JAXBElement<XMLSykepengesoeknadArbeidsgiver>
    return jaxbElement.value
}
