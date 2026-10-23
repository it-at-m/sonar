package de.muenchen.oss.sonar.backend.berechnung.domain;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaeche;
import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaechenadresse;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Mast;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungsadresse;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ZusammenfassungTest {

    private static final LocalDate BEGINN = LocalDate.of(2026, 1, 1);
    private static final LocalDate ENDE = LocalDate.of(2026, 3, 31);

    @Nested
    class Schreibe {

        @Test
        void givenAbrechnung_thenHeadItWithTheProjektnummerAndTheErsteller() {
            final Projekt projekt = new Projekt(null, "2026-0001", BEGINN, ENDE, Projekt.VERWALTUNGSGEBUEHR, List.of());
            final Flaeche flaeche = new Flaeche("Position 1", BEGINN, LocalDate.of(2026, 1, 7),
                    1, new BigDecimal("20"), new BigDecimal("20"));

            final String zusammenfassung = Zusammenfassung.schreibe(1, projekt, "mustermann", BEGINN, ENDE, 1,
                    List.of(new Flaechenadresse("Marienplatz 8", false, List.of(flaeche), 1)), List.of());

            assertThat(zusammenfassung).startsWith("Abrechnung 1 von Bauprojekt 2026-0001");
            assertThat(zusammenfassung).contains("    Ersteller Abrechnung : mustermann");
            assertThat(zusammenfassung).contains("    Anzahl Adressen      : 1");
            assertThat(zusammenfassung).contains("    Abrechnungszeitraum  : 2026-01-01 - 2026-03-31");
            assertThat(zusammenfassung).contains("    Anzahl Wochen : 1");
        }

        @Test
        void givenAdressenOfBothKinds_thenGiveEachKindItsOwnBlock() {
            final Projekt projekt = new Projekt(null, "2026-0001", BEGINN, ENDE, Projekt.VERWALTUNGSGEBUEHR, List.of());
            final Flaeche flaeche = new Flaeche("Position 1", BEGINN, LocalDate.of(2026, 1, 7),
                    1, new BigDecimal("20"), new BigDecimal("20"));
            final Mast mast = new Mast(1, BEGINN, ENDE, 1);

            final String zusammenfassung = Zusammenfassung.schreibe(1, projekt, "mustermann", BEGINN, ENDE, 1,
                    List.of(new Flaechenadresse("Marienplatz 8", false, List.of(flaeche), 1)),
                    List.of(new Ueberspannungsadresse("Marienplatz 8", List.of(mast), 3)));

            assertThat(zusammenfassung).contains("Flächen");
            assertThat(zusammenfassung).contains("Überspannungen");
            assertThat(zusammenfassung).contains("   - Fläche Position 1");
            assertThat(zusammenfassung).contains("   - Mast 1");
        }

        @Test
        void givenNoAdresseWithMasten_thenLeaveTheUeberspannungenBlockOut() {
            final Projekt projekt = new Projekt(null, "2026-0001", BEGINN, ENDE, Projekt.VERWALTUNGSGEBUEHR, List.of());
            final Flaeche flaeche = new Flaeche("Position 1", BEGINN, LocalDate.of(2026, 1, 7),
                    1, new BigDecimal("20"), new BigDecimal("20"));

            final String zusammenfassung = Zusammenfassung.schreibe(1, projekt, "mustermann", BEGINN, ENDE, 1,
                    List.of(new Flaechenadresse("Marienplatz 8", false, List.of(flaeche), 1)), List.of());

            assertThat(zusammenfassung).doesNotContain("Überspannungen");
        }

        @Test
        void givenNoAdresseAtAll_thenLeaveBothBlocksOut() {
            final Projekt projekt = new Projekt(null, "2026-0001", BEGINN, ENDE, Projekt.VERWALTUNGSGEBUEHR, List.of());

            final String zusammenfassung = Zusammenfassung.schreibe(1, projekt, "mustermann", BEGINN, ENDE, 0,
                    List.of(), List.of());

            assertThat(zusammenfassung).doesNotContain("Flächen");
            assertThat(zusammenfassung).doesNotContain("Überspannungen");
            assertThat(zusammenfassung).contains("    Anzahl Adressen      : 0");
        }
    }
}
