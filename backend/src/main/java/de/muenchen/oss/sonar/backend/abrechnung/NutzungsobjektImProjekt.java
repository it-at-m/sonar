package de.muenchen.oss.sonar.backend.abrechnung;

import java.util.UUID;

public record NutzungsobjektImProjekt(UUID projektId, NutzungsobjektEntity nutzungsobjekt) {
}
