package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import de.muenchen.oss.sonar.backend.berechnung.abschnitt.Abschnitt;
import de.muenchen.oss.sonar.backend.berechnung.abschnitt.Abschnittsbildung;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

/**
 * What the Überspannungen of one Adresse cost. They are charged Monat by Monat, and the Monate are
 * reported in the Abschnitte a Zwischenabrechnung cuts them into.
 */
public record Ueberspannungsadressabrechnung(
        String adresse,
        String berechnungslog,
        BigDecimal gebuehrUeberspannungen,
        List<BescheiddatenUeberspannung> bescheiddatenUeberspannungen) {

    private static final boolean GRUND_BEIM_EINZIGEN_ABSCHNITT = false;

    public Ueberspannungsadressabrechnung {
        bescheiddatenUeberspannungen = bescheiddatenUeberspannungen == null ? List.of() : List.copyOf(bescheiddatenUeberspannungen);
    }

    public static Ueberspannungsadressabrechnung berechne(final Ueberspannungsadresse adresse) {

        final List<Ueberspannungsmonat> monate = IntStream.rangeClosed(1, adresse.letzterMonat())
                .mapToObj(monat -> Ueberspannungsmonat.berechne(adresse, monat))
                .toList();

        final List<String> gruende = monate.stream().map(Ueberspannungsmonat::zwischenabrechnungGrund).toList();

        final List<BescheiddatenUeberspannung> abschnitte = Abschnittsbildung.abschnitte(gruende, GRUND_BEIM_EINZIGEN_ABSCHNITT).stream()
                .map(abschnitt -> bescheiddaten(abschnitt, monate))
                .toList();

        final BigDecimal gebuehr = summe(monate);

        return new Ueberspannungsadressabrechnung(adresse.adressbezeichnung(),
                Ueberspannungstext.protokoll(adresse, monate, gebuehr), gebuehr, abschnitte);
    }

    private static BescheiddatenUeberspannung bescheiddaten(final Abschnitt abschnitt, final List<Ueberspannungsmonat> monate) {

        final Ueberspannungsmonat letzterMonat = monate.get(abschnitt.bis() - 1);

        return new BescheiddatenUeberspannung(
                abschnitt.von(),
                abschnitt.bis(),
                abschnitt.dauer(),
                abschnitt.grund(),
                Ueberspannungstext.bescheidzeile(letzterMonat, abschnitt.dauer()),
                summe(monate.subList(abschnitt.von() - 1, abschnitt.bis())));
    }

    private static BigDecimal summe(final List<Ueberspannungsmonat> monate) {
        return monate.stream().map(Ueberspannungsmonat::gebuehr).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
