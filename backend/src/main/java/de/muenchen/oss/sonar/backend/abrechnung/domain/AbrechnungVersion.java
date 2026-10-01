package de.muenchen.oss.sonar.backend.abrechnung.domain;

import java.util.UUID;

public record AbrechnungVersion(UUID id, int versionsnummer, UUID vorgaengerAbrechnungId) {
}
