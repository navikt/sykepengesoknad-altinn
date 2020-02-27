package no.nav.syfo.config

import no.nav.syfo.consumer.ws.client.util.LogErrorHandler
import no.nav.syfo.consumer.ws.client.util.WsClient
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PersonConfig {

    @Bean
    fun personV3(@Value("\${virksomhet.Person.v3.endpointurl}") serviceUrl: String,
                 @Value("\${ws.sts.enabled:true}") wsStsEnabled: Boolean): PersonV3 {
        return WsClient<PersonV3>().createPort(serviceUrl, PersonV3::class.java, listOf(LogErrorHandler()), true, wsStsEnabled)
    }

}
