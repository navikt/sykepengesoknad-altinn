package no.nav.syfo.config

import no.nav.syfo.AbstractContainerBaseTest
import no.nav.syfo.Application
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [Application::class])
class ApplicationConfigTest : AbstractContainerBaseTest() {

    @Test
    fun test() {
    }
}
