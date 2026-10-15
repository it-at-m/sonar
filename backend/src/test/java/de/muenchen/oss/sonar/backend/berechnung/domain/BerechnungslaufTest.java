package de.muenchen.oss.sonar.backend.berechnung.domain;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungsArt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungMast;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungsadressabrechnung;
import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BerechnungslaufTest {

    private static final LocalDate VON = LocalDate.of(2026, 1, 1);
    private static final LocalDate BIS = LocalDate.of(2026, 3, 31);

    private static final LocalDate BERECHNUNGSDATUM = LocalDate.of(2026, 1, 7);

    @Nested
    class Run {

        @Test
        void givenNoVorabrechnung_thenStartAtLfdNrOneAndChargeTheWholeBetrag() {
            final Projekt projekt = new Projekt(null, "2026-0001", VON, BIS, Projekt.VERWALTUNGSGEBUEHR, List.of());
            final AbrechnungPosition position = new AbrechnungPosition(null, VON, LocalDate.of(2026, 1, 7),
                    new BigDecimal("10.00"), new BigDecimal("2.00"), new BigDecimal("20.00"), new BigDecimal("20.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null,
                    null, null, null, null, false, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, null, 1, null, "1000000001", false, null, null,
                    VON, BIS, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            final Berechnungsergebnis ergebnis = Berechnungslauf
                    .from(projekt, abrechnung, BERECHNUNGSDATUM, null, "mustermann")
                    .run();

            assertThat(ergebnis.lfdNr()).isEqualTo(1);
            // Gebührenstufe 1 (20 m²) and Zeitindex 1 charge 1,50 € per m² for the single Woche.
            assertThat(ergebnis.gebuehrFlaechen()).isEqualByComparingTo("30.00");
            assertThat(ergebnis.gebuehrUeberspannungen()).isEqualByComparingTo("0");
            assertThat(ergebnis.gebuehrVerwaltung()).isEqualByComparingTo(Projekt.VERWALTUNGSGEBUEHR);
            assertThat(ergebnis.gebuehrGesamt()).isEqualByComparingTo("72.00");
            assertThat(ergebnis.gebuehrZahlung()).isEqualByComparingTo(ergebnis.gebuehrGesamt());
        }

        @Test
        void givenNoVorabrechnung_thenStartTheZeitraumOnTheBeginnOfTheMassnahme() {
            final Projekt projekt = new Projekt(null, "2026-0001", VON, BIS, Projekt.VERWALTUNGSGEBUEHR, List.of());
            final AbrechnungPosition position = new AbrechnungPosition(null, VON, LocalDate.of(2026, 1, 7),
                    new BigDecimal("10.00"), new BigDecimal("2.00"), new BigDecimal("20.00"), new BigDecimal("20.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null,
                    null, null, null, null, false, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, null, 1, null, "1000000001", false, null, null,
                    VON, BIS, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            final Berechnungsergebnis ergebnis = Berechnungslauf
                    .from(projekt, abrechnung, BERECHNUNGSDATUM, null, "mustermann")
                    .run();

            assertThat(ergebnis.abrechnungszeitraumVon()).isEqualTo(VON);
            assertThat(ergebnis.abrechnungszeitraumBis()).isEqualTo(LocalDate.of(2026, 1, 7));
        }

        @Test
        void givenVorabrechnung_thenContinueItsLfdNrAndChargeOnlyWhatItLeftOver() {
            final Vorabrechnung vorabrechnung = new Vorabrechnung(1, LocalDate.of(2025, 12, 31), new BigDecimal("50.00"));
            final Projekt projekt = new Projekt(null, "2026-0001", VON, BIS, Projekt.VERWALTUNGSGEBUEHR, List.of());
            final AbrechnungPosition position = new AbrechnungPosition(null, VON, LocalDate.of(2026, 1, 7),
                    new BigDecimal("10.00"), new BigDecimal("2.00"), new BigDecimal("20.00"), new BigDecimal("20.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null,
                    null, null, null, null, false, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, null, 1, null, "1000000001", false, null, null,
                    VON, BIS, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            final Berechnungsergebnis ergebnis = Berechnungslauf
                    .from(projekt, abrechnung, BERECHNUNGSDATUM, vorabrechnung, "mustermann")
                    .run();

            assertThat(ergebnis.lfdNr()).isEqualTo(2);
            assertThat(ergebnis.gebuehrGesamt()).isEqualByComparingTo("72.00");
            assertThat(ergebnis.gebuehrZahlung()).isEqualByComparingTo("22.00");
        }

        @Test
        void givenVorabrechnung_thenStartTheZeitraumOnTheDayAfterIt() {
            final Vorabrechnung vorabrechnung = new Vorabrechnung(1, LocalDate.of(2026, 1, 3), new BigDecimal("50.00"));
            final Projekt projekt = new Projekt(null, "2026-0001", VON, BIS, Projekt.VERWALTUNGSGEBUEHR, List.of());
            final AbrechnungPosition position = new AbrechnungPosition(null, VON, LocalDate.of(2026, 1, 7),
                    new BigDecimal("10.00"), new BigDecimal("2.00"), new BigDecimal("20.00"), new BigDecimal("20.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null,
                    null, null, null, null, false, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, null, 1, null, "1000000001", false, null, null,
                    VON, BIS, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            final Berechnungsergebnis ergebnis = Berechnungslauf
                    .from(projekt, abrechnung, BERECHNUNGSDATUM, vorabrechnung, "mustermann")
                    .run();

            assertThat(ergebnis.abrechnungszeitraumVon()).isEqualTo(LocalDate.of(2026, 1, 4));
        }

        @Test
        void givenNutzungsobjektWithoutMasten_thenLeaveItOutOfTheUeberspannungen() {
            final Projekt projekt = new Projekt(null, "2026-0001", VON, BIS, Projekt.VERWALTUNGSGEBUEHR, List.of());
            final AbrechnungPosition mitMast = new AbrechnungPosition(null, VON, LocalDate.of(2026, 1, 7),
                    new BigDecimal("10.00"), new BigDecimal("2.00"), new BigDecimal("20.00"), new BigDecimal("20.00"));
            final AbrechnungPosition ohneMast = new AbrechnungPosition(null, VON, LocalDate.of(2026, 1, 7),
                    new BigDecimal("10.00"), new BigDecimal("2.00"), new BigDecimal("20.00"), new BigDecimal("20.00"));
            final AbrechnungNutzungsobjekt ueberspannt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null,
                    null, null, null, null, false, List.of(mitMast),
                    List.of(new AbrechnungMast(null, VON, BIS)));
            final AbrechnungNutzungsobjekt nurFlaeche = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Sendlinger Straße", "1", null, null, null, null, null,
                    null, null, null, null, false, List.of(ohneMast), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, null, 1, null, "1000000001", false, null, null,
                    VON, BIS, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(ueberspannt, nurFlaeche));

            final Berechnungsergebnis ergebnis = Berechnungslauf
                    .from(projekt, abrechnung, BERECHNUNGSDATUM, null, "mustermann")
                    .run();

            assertThat(ergebnis.abrechnungenUeberspannungen())
                    .extracting(Ueberspannungsadressabrechnung::adresse)
                    .containsExactly("Marienplatz 8");
        }

        @Test
        void givenAdresseBeginningLater_thenCountItsWochenFromTheEarliestBeginnOfAll() {
            final Projekt projekt = new Projekt(null, "2026-0001", VON, BIS, Projekt.VERWALTUNGSGEBUEHR, List.of());
            final AbrechnungPosition ersteWoche = new AbrechnungPosition(null, VON, LocalDate.of(2026, 1, 7),
                    new BigDecimal("10.00"), new BigDecimal("2.00"), new BigDecimal("20.00"), new BigDecimal("20.00"));
            final AbrechnungPosition zweiteWoche = new AbrechnungPosition(null, LocalDate.of(2026, 1, 8), LocalDate.of(2026, 1, 14),
                    new BigDecimal("10.00"), new BigDecimal("2.00"), new BigDecimal("20.00"), new BigDecimal("20.00"));
            final AbrechnungNutzungsobjekt frueh = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null,
                    null, null, null, null, false, List.of(ersteWoche), List.of());
            final AbrechnungNutzungsobjekt spaet = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Sendlinger Straße", "1", null, null, null, null, null,
                    null, null, null, null, false, List.of(zweiteWoche), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, null, 1, null, "1000000001", false, null, null,
                    VON, BIS, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(frueh, spaet));

            final Berechnungsergebnis ergebnis = Berechnungslauf
                    .from(projekt, abrechnung, LocalDate.of(2026, 1, 14), null, "mustermann")
                    .run();

            // The later Adresse lands in Woche 2, so both are charged once at 20 m² x 1,50 €.
            assertThat(ergebnis.gebuehrFlaechen()).isEqualByComparingTo("60.00");
            assertThat(ergebnis.abrechnungszeitraumBis()).isEqualTo(LocalDate.of(2026, 1, 14));
        }

        @Test
        void givenPositionReachingBeyondTheBerechnungsdatum_thenChargeOnlyUpToIt() {
            final Projekt projekt = new Projekt(null, "2026-0001", VON, BIS, Projekt.VERWALTUNGSGEBUEHR, List.of());
            final AbrechnungPosition position = new AbrechnungPosition(null, VON, LocalDate.of(2026, 12, 31),
                    new BigDecimal("10.00"), new BigDecimal("2.00"), new BigDecimal("20.00"), new BigDecimal("20.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null,
                    null, null, null, null, false, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, null, 1, null, "1000000001", false, null, null,
                    VON, BIS, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            final Berechnungsergebnis ergebnis = Berechnungslauf
                    .from(projekt, abrechnung, BERECHNUNGSDATUM, null, "mustermann")
                    .run();

            assertThat(ergebnis.gebuehrFlaechen()).isEqualByComparingTo("30.00");
            assertThat(ergebnis.abrechnungszeitraumBis()).isEqualTo(BERECHNUNGSDATUM);
        }

        @Test
        void givenNutzungsobjektWithoutPosition_thenChargeNoFlaeche() {
            final Projekt projekt = new Projekt(null, "2026-0001", VON, BIS, Projekt.VERWALTUNGSGEBUEHR, List.of());
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null,
                    null, null, null, null, false, List.of(), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, null, 1, null, "1000000001", false, null, null,
                    VON, BIS, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            final Berechnungsergebnis ergebnis = Berechnungslauf
                    .from(projekt, abrechnung, BERECHNUNGSDATUM, null, "mustermann")
                    .run();

            assertThat(ergebnis.gebuehrFlaechen()).isEqualByComparingTo("0");
            assertThat(ergebnis.gebuehrGesamt()).isEqualByComparingTo(Projekt.VERWALTUNGSGEBUEHR);
            assertThat(ergebnis.bescheiddatenFlaechen()).singleElement()
                    .extracting(bescheiddaten -> bescheiddaten.wocheDauer()).isEqualTo(0);
        }

        @Test
        void givenAbrechnung_thenWriteTheZusammenfassungAndTheBerechnungslog() {
            final Projekt projekt = new Projekt(null, "2026-0001", VON, BIS, Projekt.VERWALTUNGSGEBUEHR, List.of());
            final AbrechnungPosition position = new AbrechnungPosition(null, VON, LocalDate.of(2026, 1, 7),
                    new BigDecimal("10.00"), new BigDecimal("2.00"), new BigDecimal("20.00"), new BigDecimal("20.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null,
                    null, null, null, null, false, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, null, 1, null, "1000000001", false, null, null,
                    VON, BIS, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            final Berechnungsergebnis ergebnis = Berechnungslauf
                    .from(projekt, abrechnung, BERECHNUNGSDATUM, null, "mustermann")
                    .run();

            assertThat(ergebnis.zusammenfassung()).startsWith("Abrechnung 1 von Bauprojekt 2026-0001");
            assertThat(ergebnis.zusammenfassung()).contains("mustermann");
            assertThat(ergebnis.berechnungslog()).contains("====>>>> Die Gesamtgebühren betragen 72,00€");
        }
    }
}
