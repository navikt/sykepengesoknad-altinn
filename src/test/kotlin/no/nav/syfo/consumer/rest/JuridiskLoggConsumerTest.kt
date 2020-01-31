package no.nav.syfo.consumer.rest

import no.nav.syfo.consumer.rest.juridisklogg.JuridiskLoggConsumer
import no.nav.syfo.consumer.rest.juridisklogg.JuridiskLoggException
import no.nav.syfo.consumer.rest.juridisklogg.JuridiskRespose
import no.nav.syfo.consumer.rest.juridisklogg.Logg
import no.nav.syfo.domain.AltinnInnsendelseEkstraData
import no.nav.syfo.mockSykepengesoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito
import org.mockito.BDDMockito.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@RunWith(MockitoJUnitRunner::class)
class JuridiskLoggConsumerTest {

    @Mock
    private lateinit var basicAuthRestTemplate: RestTemplate

    private lateinit var juridiskLoggConsumer: JuridiskLoggConsumer

    @Before
    fun setup() {
        given(basicAuthRestTemplate.exchange(
                BDDMockito.anyString(),
                any(HttpMethod::class.java),
                any(HttpEntity::class.java),
                BDDMockito.eq(JuridiskRespose::class.java)
        )).willReturn(ResponseEntity(JuridiskRespose(123), HttpStatus.OK))

        juridiskLoggConsumer = JuridiskLoggConsumer(basicAuthRestTemplate, "url", "username")
    }

    @Test
    fun lagrerIJurdiskLogg() {
        val ref = juridiskLoggConsumer.lagreIJuridiskLogg(mockSykepengesoknad.first, 123, mockSykepengesoknad.second)
        assertThat(ref).isEqualTo(123)
    }

    @Test(expected = JuridiskLoggException::class)
    fun responsForskjelligFra200KasterFeil() {
        given(basicAuthRestTemplate.exchange(
                BDDMockito.anyString(),
                any(HttpMethod::class.java),
                any(HttpEntity::class.java),
                BDDMockito.eq(JuridiskRespose::class.java)
        )).willReturn(ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR))

        juridiskLoggConsumer.lagreIJuridiskLogg(mockSykepengesoknad.first, 123, mockSykepengesoknad.second)
    }

    @Test(expected = JuridiskLoggException::class)
    fun clientErrorLoggesOgKastesVidere() {
        given(basicAuthRestTemplate.exchange(
                BDDMockito.anyString(),
                any(HttpMethod::class.java),
                any(HttpEntity::class.java),
                BDDMockito.eq(JuridiskRespose::class.java)
        )).willThrow(HttpClientErrorException(HttpStatus.BAD_REQUEST, "", "Payload må være base64-encodet".toByteArray(charset("UTF-8")), null))

        juridiskLoggConsumer.lagreIJuridiskLogg(mockSykepengesoknad.first, 123, mockSykepengesoknad.second)
    }

    /* Denne testen vil brekke om innholdet i xml-utgaven av søknaden endrer seg. Da må en bumpe versjonen i metadata
     * og bytte ut hashen med den nye. Det er så vi skal ha mulighet til å gå tilbake og regenerere en slik hash i en
     * evt. juridisk tvist hvor vi må bevise at en søknad med innhold X faktisk er sendt til Altinn
     */
    @Test
    fun sjekkInnholdIPayload() {
        val argumentCaptor = ArgumentCaptor.forClass(HttpEntity::class.java)

        juridiskLoggConsumer.lagreIJuridiskLogg(mockSykepengesoknad.first, 123, mockSykepengesoknad.second)

        Mockito.verify(basicAuthRestTemplate).exchange(
                BDDMockito.anyString(),
                any(HttpMethod::class.java),
                argumentCaptor.capture(),
                BDDMockito.eq(JuridiskRespose::class.java))

        assertThat((argumentCaptor.value.body as Logg).meldingsInnhold).isEqualTo("" +
                "aGFzaDogVjU7YWx0aW5uS3ZpdHRlcmluZzogMTIzOnpBD++/ve+/vUASEu+/ve+/vWRS77+9TShB77+977+977+977+977+" +
                "9XjXRr++/ve+/vUTvv70u77+9NAtoa++/vUnvv71j77+977+9Vu+/vWg6bO+/vQgx77+9M++/ve+/ve+/vSHvv73LoAF2Yn3vv719")
    }
}
