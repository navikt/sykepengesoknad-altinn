package no.nav.syfo

import no.nav.melding.virksomhet.sykepengesoeknadarbeidsgiver.v1.sykepengesoeknadarbeidsgiver.XMLSykepengesoeknadArbeidsgiver;
import no.nav.syfo.domain.soknad.Sykepengesoknad

class SykepengesoknadAltinn constructor(var sykepengesoknad: Sykepengesoknad) {
    val log = log()

    var sykepengesoknadArbeidsgiver: XMLSykepengesoeknadArbeidsgiver
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
