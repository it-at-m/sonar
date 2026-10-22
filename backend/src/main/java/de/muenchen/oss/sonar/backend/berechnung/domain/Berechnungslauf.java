package de.muenchen.oss.sonar.backend.berechnung.domain;

import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaechenadresse;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungsadresse;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import java.util.List;

public record Berechnungslauf(
        Projekt projekt,
        String username,
        Vorabrechnung vorabrechnung,
        List<Flaechenadresse> flaechenadressen,
        List<Ueberspannungsadresse> ueberspannungsadressen,
        Massnahmezeitraum zeitraum) {

    public Berechnungslauf {
        flaechenadressen = List.copyOf(flaechenadressen);
        ueberspannungsadressen = List.copyOf(ueberspannungsadressen);
    }
}
