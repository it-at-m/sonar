package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;

public record VorgaengerPosition(String projektnummer, AbrechnungPosition position) {
}
