package de.muenchen.oss.sonar.backend.projekt;

import de.muenchen.oss.sonar.backend.stadtbezirk.Stadtbezirk;
import java.util.UUID;

public record ProjektStadtbezirk(UUID id, Stadtbezirk stadtbezirk) {
}
