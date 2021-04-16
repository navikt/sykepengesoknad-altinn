package no.nav.syfo.consumer.ws.client

import no.nav.syfo.log
import no.nav.tjeneste.virksomhet.organisasjon.v4.HentOrganisasjonOrganisasjonIkkeFunnet
import no.nav.tjeneste.virksomhet.organisasjon.v4.HentOrganisasjonUgyldigInput
import no.nav.tjeneste.virksomhet.organisasjon.v4.OrganisasjonV4
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.*
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.WSHentOrganisasjonRequest
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.inject.Inject
import javax.xml.datatype.XMLGregorianCalendar

@Component
class OrganisasjonConsumer @Inject
constructor(private val organisasjonV4: OrganisasjonV4) {
    val log = log()

    fun hentJuridiskOrgnummer(virksomhetsnummer: String): String? {
        val wsOrganisasjon = hentOrganisasjon(virksomhetsnummer)
        val hentJuridiskOrgnummer = hentJuridiskOrgnummer(wsOrganisasjon)

        return hentJuridiskOrgnummer
    }

    @Cacheable(cacheNames = ["organisasjon"])
    fun hentOrganisasjon(orgnummer: String): WSOrganisasjon {
        try {
            return organisasjonV4.hentOrganisasjon(
                WSHentOrganisasjonRequest()
                    .withOrgnummer(orgnummer)
                    .withInkluderHierarki(true)
                    .withInkluderHistorikk(true)
            ).organisasjon
        } catch (e: HentOrganisasjonOrganisasjonIkkeFunnet) {
            log.warn("Kunne ikke hente organisasjon for {}", orgnummer, e)
            throw RuntimeException("Kunne ikke hente organisasjon for $orgnummer")
        } catch (e: HentOrganisasjonUgyldigInput) {
            log.warn("Kunne ikke hente organisasjon for {}", orgnummer, e)
            throw RuntimeException("Kunne ikke hente organisasjon for $orgnummer")
        } catch (e: RuntimeException) {
            log.error("Feil ved henting av Organisasjon", e)
            throw RuntimeException("Kunne ikke hente organisasjon for $orgnummer")
        }
    }

    private fun hentJuridiskOrgnummer(wsOrganisasjon: WSOrganisasjon): String? {

        val juridiskOrgnummer = finnJuridiskOrgnummer(wsOrganisasjon)

        if (juridiskOrgnummer == null) {
            log.warn("Finner ikke juridisk orgnummer for virksomhetsnummer " + wsOrganisasjon.orgnummer + " i organisasjonstreet")
        }

        return juridiskOrgnummer
    }

    private fun finnJuridiskOrgnummer(wsOrganisasjon: WSOrganisasjon): String? {
        return wsOrganisasjon
            .takeIf { it is WSVirksomhet }
            .let { it as WSVirksomhet }
            .let(this::finnJuridiskOrgnummer)
            .map(WSInngaarIJuridiskEnhet::getJuridiskEnhet)
            .map(WSJuridiskEnhet::getOrgnummer)
            .firstOrNull()
    }

    private fun finnJuridiskOrgnummer(virksomhet: WSVirksomhet): List<WSInngaarIJuridiskEnhet> {
        val juridiskEnhetListe = filtrerGyldigeEnheter(virksomhet.inngaarIJuridiskEnhet)
        return if (!juridiskEnhetListe.isEmpty()) {
            juridiskEnhetListe
        } else finnJuridiskOrgnummer(virksomhet.bestaarAvOrgledd)
    }

    private fun finnJuridiskOrgnummer(bestaarAvOrgledd: List<WSBestaarAvOrgledd>): List<WSInngaarIJuridiskEnhet> {
        return bestaarAvOrgledd
            .map(WSBestaarAvOrgledd::getOrgLedd)
            .map(this::finnJuridiskOrgnummer)
            .flatten()
    }

    private fun finnJuridiskOrgnummer(orgledd: WSOrgledd): List<WSInngaarIJuridiskEnhet> {
        val juridiskEnhetListe = filtrerGyldigeEnheter(orgledd.inngaarIJuridiskEnhet)
        return if (!juridiskEnhetListe.isEmpty()) {
            juridiskEnhetListe
        } else finnJuridiskOrgnummer(orgledd.overOrgledd)
    }

    private fun filtrerGyldigeEnheter(inngaarIJuridiskEnhetListe: List<WSInngaarIJuridiskEnhet>): List<WSInngaarIJuridiskEnhet> {
        return inngaarIJuridiskEnhetListe
            .filter { wsInngaarIJuridiskEnhet: WSInngaarIJuridiskEnhet ->
                wsInngaarIJuridiskEnhet.tomGyldighetsperiode == null ||
                    !convertToLocalDate(wsInngaarIJuridiskEnhet.tomGyldighetsperiode).isBefore(LocalDate.now())
            }
    }

    private fun convertToLocalDate(xmlGregorianCalendar: XMLGregorianCalendar): LocalDate {
        return xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime().toLocalDate()
    }
}
