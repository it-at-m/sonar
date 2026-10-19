package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * One Adresse as the Überspannungen are charged on it: the Masten overspanning it, in the order
 * they were entered. The Bescheid reports the Überspannungen per Adresse and not per Monat.
 */
public record Ueberspannungsadresse(
        String adressbezeichnung,
        List<Mast> masten,
        int letzterMonat) {

    public Ueberspannungsadresse {
        masten = List.copyOf(masten);
    }

    public Optional<LocalDate> earliestStart() {
        return masten.stream().map(Mast::beginn).min(Comparator.naturalOrder());
    }

    public Optional<LocalDate> latestEnd() {
        return masten.stream().map(Mast::ende).max(Comparator.naturalOrder());
    }
}
