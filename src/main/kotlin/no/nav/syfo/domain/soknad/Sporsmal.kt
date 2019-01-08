package no.nav.syfo.domain.soknad

data class Sporsmal(

        val id: String? = null,
        val tag: String? = null,
        val sporsmalstekst: String? = null,
        val undertekst: String? = null,
        val svartype: Svartype? = null,
        val min: String? = null,
        val max: String? = null,
        val kriterieForVisningAvUndersporsmal: Visningskriterium? = null,
        val svar: List<Svar> = arrayListOf(),
        val undersporsmal: List<Sporsmal> = arrayListOf()
)
