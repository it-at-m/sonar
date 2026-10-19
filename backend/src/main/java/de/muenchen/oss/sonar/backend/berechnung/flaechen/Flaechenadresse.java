package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record Flaechenadresse(
        String adressbezeichnung,
        boolean isAufschlag50Prozent,
        List<Flaeche> flaechen,
        int letzteWoche) {

    public Flaechenadresse {
        flaechen = List.copyOf(flaechen);
    }

    public Optional<LocalDate> beginn() {
        return flaechen.stream().map(Flaeche::beginn).min(Comparator.naturalOrder());
    }

    public Optional<LocalDate> ende() {
        return flaechen.stream().map(Flaeche::berechnungsEnde).max(Comparator.naturalOrder());
    }
}
