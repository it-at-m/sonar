package de.muenchen.oss.sonar.backend.projekt.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record VorgaengerProjektRequestDTO(@NotNull UUID vorgaengerProjektId) {
}
