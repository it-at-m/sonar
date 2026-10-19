package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UeberspannungsabrechnungService {

    public static Ueberspannungsabrechnung berechne(final List<Ueberspannungsadresse> adressen) {

        final List<Ueberspannungsadressabrechnung> adressabrechnungen = adressen.stream()
                .map(UeberspannungsadressabrechnungService::berechne)
                .toList();

        return new Ueberspannungsabrechnung(adressabrechnungen, summe(adressabrechnungen));
    }

    private static BigDecimal summe(final List<Ueberspannungsadressabrechnung> adressabrechnungen) {
        return adressabrechnungen.stream()
                .map(Ueberspannungsadressabrechnung::gebuehr)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
