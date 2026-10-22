package de.muenchen.oss.sonar.backend.berechnung.domain;

import java.time.LocalDate;

public record Massnahmezeitraum(LocalDate beginn, LocalDate ende, int anzahlWochen) {
}
