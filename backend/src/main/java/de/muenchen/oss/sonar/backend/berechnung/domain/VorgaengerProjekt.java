package de.muenchen.oss.sonar.backend.berechnung.domain;

import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import java.util.List;

public record VorgaengerProjekt(String projektnummer, List<AbrechnungNutzungsobjekt> nutzungsobjekte) {

    public VorgaengerProjekt {
        nutzungsobjekte = List.copyOf(nutzungsobjekte);
    }
}
