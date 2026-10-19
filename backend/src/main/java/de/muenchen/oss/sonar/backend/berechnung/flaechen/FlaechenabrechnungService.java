package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import de.muenchen.oss.sonar.backend.berechnung.abschnitt.Abschnitt;
import de.muenchen.oss.sonar.backend.berechnung.abschnitt.Abschnittsbildung;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlaechenabrechnungService {

    private static final boolean GRUND_BEIM_EINZIGEN_ABSCHNITT = true;

    public static Flaechenabrechnung berechne(final List<Flaechenadresse> adressen, final int anzahlWochen) {

        if (anzahlWochen == 0) {
            return new Flaechenabrechnung(List.of(), List.of(abschnittOhneWoche()), BigDecimal.ZERO);
        }

        final List<Flaechenwoche> wochen = IntStream.rangeClosed(1, anzahlWochen)
                .mapToObj(woche -> FlaechenwocheService.berechne(adressen, woche))
                .toList();

        final List<String> gruende = wochen.stream().map(Flaechenwoche::zwischenabrechnungGrund).toList();

        final List<BescheiddatenFlaeche> abschnitte = Abschnittsbildung.abschnitte(gruende, GRUND_BEIM_EINZIGEN_ABSCHNITT).stream()
                .map(abschnitt -> bescheiddaten(abschnitt, wochen))
                .toList();

        return new Flaechenabrechnung(wochen, abschnitte, summe(wochen));
    }

    private static BescheiddatenFlaeche bescheiddaten(final Abschnitt abschnitt, final List<Flaechenwoche> wochen) {

        final Flaechenwoche letzteWoche = wochen.get(abschnitt.bis() - 1);
        final StringBuilder berechnung = new StringBuilder();

        for (final Gebuehrenposten posten : letzteWoche.posten()) {
            berechnung.append(Flaechentext.bescheidzeile(posten, abschnitt.dauer()));
        }

        return new BescheiddatenFlaeche(
                abschnitt.von(),
                abschnitt.bis(),
                abschnitt.dauer(),
                Flaechentext.verteilung(letzteWoche),
                abschnitt.grund(),
                Flaechentext.gebuehrenstufe(letzteWoche),
                berechnung.toString(),
                summe(wochen.subList(abschnitt.von() - 1, abschnitt.bis())));
    }

    private static BescheiddatenFlaeche abschnittOhneWoche() {
        return new BescheiddatenFlaeche(1, 0, 0, "", "",
                Flaechentext.gebuehrenstufeOhneWoche(), "", BigDecimal.ZERO);
    }

    private static BigDecimal summe(final List<Flaechenwoche> wochen) {
        return wochen.stream().map(Flaechenwoche::gebuehr).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
