package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungMast;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UeberspannungsadresseService {

    public static Ueberspannungsadresse berechne(final AbrechnungNutzungsobjekt nutzungsobjekt) {

        final List<AbrechnungMast> erfasst = nutzungsobjekt.masten();

        final LocalDate beginnAdresse = erfasst.stream().map(AbrechnungMast::beginn).min(Comparator.naturalOrder()).orElse(null);

        final List<Mast> masten = new ArrayList<>();
        for (final AbrechnungMast mast : erfasst) {
            masten.add(MastService.berechne(mast, masten.size() + 1, beginnAdresse));
        }

        final int letzterMonat = masten.stream().mapToInt(Mast::letzterMonat).max().orElse(0);

        return new Ueberspannungsadresse(nutzungsobjekt.adressbezeichnung(), masten, letzterMonat);
    }

    public static List<Mast> activeMastenOldestFirst(final Ueberspannungsadresse adresse, final int monat) {
        return adresse.masten().stream()
                .filter(mast -> mast.isActive(monat))
                // The oldest Mast is the erste Mast, so the position in this list becomes the Rang.
                .sorted(Comparator.comparingInt((final Mast mast) -> mast.runningMonth(monat)).reversed())
                .toList();
    }
}
