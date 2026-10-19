package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UeberspannungsabrechnungService {

    public static Ueberspannungsabrechnung berechne(final List<Ueberspannungsadresse> ueberspannungsadressen) {

        final List<Ueberspannungsadressabrechnung> adressen = ueberspannungsadressen.stream()
                .map(UeberspannungsadressabrechnungService::berechne)
                .toList();

        return new Ueberspannungsabrechnung(adressen, summe(adressen));
    }

    private static BigDecimal summe(final List<Ueberspannungsadressabrechnung> adressen) {
        return adressen.stream()
                .map(Ueberspannungsadressabrechnung::gebuehrUeberspannungen)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
