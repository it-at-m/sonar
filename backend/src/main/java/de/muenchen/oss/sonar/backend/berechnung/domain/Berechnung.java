package de.muenchen.oss.sonar.backend.berechnung.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Berechnung {

    private LocalDate abrechnungszeitraumVon;

    private LocalDate abrechnungszeitraumBis;

    private BigDecimal gebuehrVerwaltung;

    private String zusammenfassung;

}
