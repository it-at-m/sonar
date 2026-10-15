package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UeberspannungstextTest {

    private static final String NL = System.lineSeparator();

    @Nested
    class Zusammenfassung {

        @Test
        void givenMast_thenWriteItsZeitraumAndItsGueltigkeit() {
            final Mast mast = new Mast(2, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3, 6);

            assertThat(Ueberspannungstext.zusammenfassung(mast)).isEqualTo(
                    "   - Mast 2" + NL
                            + "        Beginn: 2026-03-01  Ende: 2026-06-30 (--> 4 Monate)" + NL
                            + "        Gültigkeit: Monat 3-6" + NL);
        }

        @Test
        void givenAdresse_thenWriteTheAnzahlMonateAndABlockPerMast() {
            final Mast erster = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1, 6);
            final Mast zweiter = new Mast(2, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3, 6);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(erster, zweiter), 6);

            assertThat(Ueberspannungstext.zusammenfassung(adresse)).isEqualTo(
                    "Marienplatz 8" + NL
                            + "Anzahl Monate: 6" + NL
                            + "   - Mast 1" + NL
                            + "        Beginn: 2026-01-01  Ende: 2026-06-30 (--> 6 Monate)" + NL
                            + "        Gültigkeit: Monat 1-6" + NL
                            + "   - Mast 2" + NL
                            + "        Beginn: 2026-03-01  Ende: 2026-06-30 (--> 4 Monate)" + NL
                            + "        Gültigkeit: Monat 3-6" + NL
                            + NL);
        }
    }

    @Nested
    class Protokoll {

        @Test
        void givenMonate_thenWriteEveryMastAndCloseWithTheGebuehrOfTheAdresse() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28), 1, 2);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(mast), 2);
            final List<Ueberspannungsmonat> monate = List.of(
                    new Ueberspannungsmonat(1, List.of(new Mastposten(1, mast, 1)), ""),
                    new Ueberspannungsmonat(2, List.of(new Mastposten(1, mast, 2)), ""));

            assertThat(Ueberspannungstext.protokoll(adresse, monate, new BigDecimal("100.00"))).isEqualTo(
                    "Monat 1" + NL
                            + "    Anzahl Masten: 1" + NL
                            + "    Mast 1: 1. Monat  --> Gebührensatz: 50,00€" + NL
                            + "    ==> Die Gebühr für Monat 1 beträgt 50,00€" + NL
                            + NL
                            + "Monat 2" + NL
                            + "    Anzahl Masten: 1" + NL
                            + "    Mast 1: 2. Monat  --> Gebührensatz: 50,00€" + NL
                            + "    ==> Die Gebühr für Monat 2 beträgt 50,00€" + NL
                            + NL
                            + "===> Die Gebühr für Adresse Marienplatz 8 beträgt 100,00€" + NL
                            + NL);
        }
    }

    @Nested
    class Bescheidzeile {

        @Test
        void givenAbschnitt_thenWriteTheDauerTheSatzAndTheAnzahlMasten() {
            final Mast erster = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1, 6);
            final Mast zweiter = new Mast(2, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3, 6);
            final Ueberspannungsmonat monat = new Ueberspannungsmonat(
                    6, List.of(new Mastposten(1, erster, 6), new Mastposten(2, zweiter, 4)), "");

            assertThat(Ueberspannungstext.bescheidzeile(monat, 4)).isEqualTo("    4 Monate x 50,00€ (2 Masten)" + NL);
        }
    }
}
