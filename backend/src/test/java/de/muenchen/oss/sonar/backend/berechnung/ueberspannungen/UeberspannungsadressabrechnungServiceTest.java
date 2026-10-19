package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UeberspannungsadressabrechnungServiceTest {

    @Nested
    class Berechne {

        @Test
        void givenOneMastOverThreeMonate_thenChargeTheVollerSatzPerMonat() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(mast), 3);

            final Ueberspannungsadressabrechnung abrechnung = UeberspannungsadressabrechnungService.berechne(adresse);

            assertThat(abrechnung.gebuehrUeberspannungen()).isEqualTo(new BigDecimal("150.00"));
        }

        @Test
        void givenOneMastThroughout_thenReportASingleAbschnittWithoutGrund() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(mast), 3);

            final Ueberspannungsadressabrechnung abrechnung = UeberspannungsadressabrechnungService.berechne(adresse);

            assertThat(abrechnung.bescheiddatenUeberspannungen()).containsExactly(
                    new BescheiddatenUeberspannung(1, 3, 3, "",
                            "    3 Monate x 50,00€ (1 Masten)" + System.lineSeparator(), new BigDecimal("150.00")));
        }

        @Test
        void givenMastJoiningLater_thenCutAnAbschnittAtThatMonat() {
            final Mast laufend = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1);
            final Mast neu = new Mast(2, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(laufend, neu), 6);

            final Ueberspannungsadressabrechnung abrechnung = UeberspannungsadressabrechnungService.berechne(adresse);

            assertThat(abrechnung.bescheiddatenUeberspannungen()).containsExactly(
                    new BescheiddatenUeberspannung(1, 2, 2, "",
                            "    2 Monate x 50,00€ (1 Masten)" + System.lineSeparator(), new BigDecimal("100.00")),
                    new BescheiddatenUeberspannung(3, 6, 4,
                            "    Anzahl der Masten hat sich geändert auf 2" + System.lineSeparator(),
                            "    4 Monate x 50,00€ (2 Masten)" + System.lineSeparator(), new BigDecimal("200.00")));
        }

        @Test
        void givenZweiterMast_thenChargeNothingForIt() {
            final Mast laufend = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1);
            final Mast neu = new Mast(2, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(laufend, neu), 6);

            final Ueberspannungsadressabrechnung abrechnung = UeberspannungsadressabrechnungService.berechne(adresse);

            assertThat(abrechnung.gebuehrUeberspannungen()).isEqualTo(new BigDecimal("300.00"));
        }

        @Test
        void givenMastenReachingTheSecondAltersstufe_thenCutAnAbschnittAndChargeTheHoehererSatz() {
            final Mast erster = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 2, 28), 1);
            final Mast zweiter = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 2, 28), 1);
            final Mast dritter = new Mast(3, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 2, 28), 1);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(erster, zweiter, dritter), 14);

            final Ueberspannungsadressabrechnung abrechnung = UeberspannungsadressabrechnungService.berechne(adresse);

            assertThat(abrechnung.bescheiddatenUeberspannungen()).containsExactly(
                    new BescheiddatenUeberspannung(1, 12, 12, "",
                            "    12 Monate x 65,00€ (3 Masten)" + System.lineSeparator(), new BigDecimal("780.00")),
                    new BescheiddatenUeberspannung(13, 14, 2,
                            "    Mast 1 wechselt in Altersstufe 2" + System.lineSeparator()
                                    + "    Mast 2 wechselt in Altersstufe 2" + System.lineSeparator()
                                    + "    Mast 3 wechselt in Altersstufe 2" + System.lineSeparator(),
                            "    2 Monate x 72,50€ (3 Masten)" + System.lineSeparator(), new BigDecimal("145.00")));
            assertThat(abrechnung.gebuehrUeberspannungen()).isEqualTo(new BigDecimal("925.00"));
        }

        @Test
        void givenAdresse_thenLabelTheAbrechnungWithIt() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Flurstück 123, Sendling", List.of(mast), 3);

            final Ueberspannungsadressabrechnung abrechnung = UeberspannungsadressabrechnungService.berechne(adresse);

            assertThat(abrechnung.adresse()).isEqualTo("Flurstück 123, Sendling");
        }

        @Test
        void givenAdresse_thenCloseItsBerechnungslogWithTheGebuehr() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(mast), 3);

            final Ueberspannungsadressabrechnung abrechnung = UeberspannungsadressabrechnungService.berechne(adresse);

            assertThat(abrechnung.berechnungslog())
                    .contains("===> Die Gebühr für Adresse Marienplatz 8 beträgt 150,00€");
        }
    }
}
