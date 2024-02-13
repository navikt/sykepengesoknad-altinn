package no.nav.syfo

import no.nav.syfo.FellesTestOppsett.Companion.pdlMockWebserver
import no.nav.syfo.client.pdl.GetPersonResponse
import no.nav.syfo.client.pdl.HentPerson
import no.nav.syfo.client.pdl.Navn
import no.nav.syfo.client.pdl.ResponseData
import okhttp3.mockwebserver.MockResponse
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType

fun mockPdlResponse(
    hentNavnResponse: GetPersonResponse =
        GetPersonResponse(
            errors = emptyList(),
            data =
                ResponseData(
                    hentPerson =
                        HentPerson(
                            navn =
                                listOf(
                                    Navn(fornavn = "OLE", mellomnavn = null, etternavn = "GUNNAR"),
                                ),
                        ),
                ),
        ),
) {
    val response =
        MockResponse()
            .setBody(hentNavnResponse.serialisertTilString())
            .setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

    pdlMockWebserver.enqueue(response)
}
