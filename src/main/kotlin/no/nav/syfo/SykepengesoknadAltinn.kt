package no.nav.syfo

import no.nav.melding.virksomhet.sykepengesoeknadarbeidsgiver.v1.sykepengesoeknadarbeidsgiver.ObjectFactory
import no.nav.melding.virksomhet.sykepengesoeknadarbeidsgiver.v1.sykepengesoeknadarbeidsgiver.XMLSykepengesoeknadArbeidsgiver;
import no.nav.syfo.domain.soknad.Sykepengesoknad
import no.nav.syfo.util.JAXB.marshallSykepengesoeknadArbeidsgiver
import javax.xml.bind.ValidationEvent

class SykepengesoknadAltinn(val sykepengesoknad: Sykepengesoknad,
                            val fnr: String,
                            val navn: String?,
                            val xmlSykepengesoeknadArbeidsgiver: XMLSykepengesoeknadArbeidsgiver,
                            val pdf: ByteArray) {

    var validationEventer: MutableList<ValidationEvent> = mutableListOf()
    val xml: ByteArray

    init {
        xml = marshallSykepengesoeknadArbeidsgiver(
                ObjectFactory().createSykepengesoeknadArbeidsgiver(xmlSykepengesoeknadArbeidsgiver)
        ) { event ->
            validationEventer.add(event)
            true
        }.toByteArray()
    }

}

/*var sykepengesoknadArbeidsgiver: XMLSykepengesoeknadArbeidsgiver
//var validationEventer: MutableList<ValidationEvent> = ArrayList()
//var sykepengesoeknadXML: String
private var hashSykepengesoeknadXML: String? = null
private var sykepengesoeknadPDF: ByteArray? = null

init {
     sykepengesoknadArbeidsgiver = sykepengesoeknadArbeidsgiver2XML(sykepengesoknad)
    //this.sykepengesoknadArbeidsgiver = map(sykepengesoknad, sykepengesoeknadArbeidsgiver2XML)

    /*val validationEventHandler = { event ->
        this.validationEventer.add(event)
        true
    }*/
    /*this.sykepengesoeknadXML = marshallSykepengesoeknadArbeidsgiver(
            ObjectFactory().createSykepengesoeknadArbeidsgiver(sykepengesoknadArbeidsgiver),
            validationEventHandler)*/
}

//TODO denne skal lagres i juridisk logg
/*fun getHashSykepengesoeknadXML(): String? {
    if (hashSykepengesoeknadXML == null) {
        hashSykepengesoeknadXML = sha512AsBase64String(sykepengesoeknadXML)
    }
    return hashSykepengesoeknadXML
}*/


}
*/