package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungMast;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import java.time.LocalDate;
import java.util.ArrayList;
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

    public static Ueberspannungsadresse from(final AbrechnungNutzungsobjekt nutzungsobjekt) {

        final List<AbrechnungMast> erfasst = nutzungsobjekt.masten();

        final LocalDate beginnAdresse = erfasst.stream().map(AbrechnungMast::beginn).min(Comparator.naturalOrder()).orElse(null);

        final List<Mast> masten = new ArrayList<>();
        for (final AbrechnungMast mast : erfasst) {
            masten.add(Mast.from(mast, masten.size() + 1, beginnAdresse));
        }

        final int letzterMonat = masten.stream().mapToInt(Mast::letzterMonat).max().orElse(0);

        return new Ueberspannungsadresse(nutzungsobjekt.adressbezeichnung(), masten, letzterMonat);
    }

    public Optional<LocalDate> earliestStart() {
        return masten.stream().map(Mast::beginn).min(Comparator.naturalOrder());
    }

    public Optional<LocalDate> latestEnd() {
        return masten.stream().map(Mast::ende).max(Comparator.naturalOrder());
    }

    public List<Mast> activeMastenOldestFirst(final int monat) {
        return masten.stream()
                .filter(mast -> mast.isActive(monat))
                // The oldest Mast is the erste Mast, so the position in this list becomes the Rang.
                .sorted(Comparator.comparingInt((final Mast mast) -> mast.runningMonth(monat)).reversed())
                .toList();
    }

}
