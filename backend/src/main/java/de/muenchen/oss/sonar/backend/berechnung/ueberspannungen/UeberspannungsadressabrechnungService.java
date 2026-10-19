package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import de.muenchen.oss.sonar.backend.berechnung.abschnitt.Abschnitt;
import de.muenchen.oss.sonar.backend.berechnung.abschnitt.Abschnittsbildung;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UeberspannungsadressabrechnungService {

    private static final boolean GRUND_BEIM_EINZIGEN_ABSCHNITT = false;

    public static Ueberspannungsadressabrechnung berechne(final Ueberspannungsadresse adresse) {

        final List<Ueberspannungsmonat> monate = IntStream.rangeClosed(1, adresse.letzterMonat())
                .mapToObj(monat -> UeberspannungsmonatService.berechne(adresse, monat))
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
