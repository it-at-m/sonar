package de.muenchen.oss.sonar.backend.projekt;

import java.util.UUID;

public record VorgaengerVorschlag(UUID projektId, ProjektEntity vorgaenger) {
}
