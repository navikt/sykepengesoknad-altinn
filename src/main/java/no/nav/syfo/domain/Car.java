package no.nav.syfo.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.time.LocalDate;

@Builder
@Value
@Getter
public class Car {
    private String uuid;
    private String color;
    private String brand;
    private LocalDate bought;
}
