package no.nav.syfo.controller;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.kafka.TestProducer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping(value = "/test")
public class KafkaTestController {

    private TestProducer testProducer;

    public KafkaTestController(TestProducer testProducer) {
        this.testProducer = testProducer;
    }

    @ResponseBody
    @RequestMapping(value = "/produce", produces = MediaType.TEXT_PLAIN_VALUE)
    public String produce() {
        testProducer.sendMelding();

        return "Lagt testdata på topic 🚀";
    }
}
