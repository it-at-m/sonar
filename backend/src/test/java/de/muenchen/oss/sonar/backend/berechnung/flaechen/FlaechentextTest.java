package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import static de.muenchen.oss.sonar.backend.berechnung.gebuehren.Flaechengroessenstufe.STUFE_2;
import static de.muenchen.oss.sonar.backend.berechnung.gebuehren.Flaechenzeitindex.ZEITINDEX_1;
import static de.muenchen.oss.sonar.backend.berechnung.gebuehren.Flaechenzeitindex.ZEITINDEX_2;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FlaechentextTest {

    private static final String NEWLINE = System.lineSeparator();

    @Nested
    class Zusammenfassung {

        @Nested
        class OfFlaechenadresse {

            @Test
            void givenAdresseWithOneFlaeche_thenWriteTheWholeBlock() {
                final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15), 1,
                        new BigDecimal("100"), new BigDecimal("100"));
                final Flaechenadresse adresse = new Flaechenadresse("Marienplatz 8", false, List.of(flaeche), 3);

                assertThat(Flaechentext.zusammenfassung(adresse)).isEqualTo(
                        "Marienplatz 8" + NEWLINE
                                + "   50%-Aufschlag : false" + NEWLINE
                                + "   Beginn : 2026-01-01 (Woche : 1)" + NEWLINE
                                + "   Ende : 2026-01-15 (Woche : 3)" + NEWLINE
                                + "   - Fläche Position 1" + NEWLINE
                                + "        Beginn: 2026-01-01  Ende: 2026-01-15 (15 Tage --> 3 Wochen)" + NEWLINE
                                + "        Gesamtfläche Adresse: 100m²" + NEWLINE
                                + "        Flächenänderung: +100m²" + NEWLINE
                                + "        Gültigkeit: Woche 1-3" + NEWLINE
                                + NEWLINE);
            }

            @Test
            void givenAdresseWithAufschlag_thenSaySo() {
                final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15), 1,
                        new BigDecimal("100"), new BigDecimal("100"));
                final Flaechenadresse adresse = new Flaechenadresse("Marienplatz 8", true, List.of(flaeche), 3);

                assertThat(Flaechentext.zusammenfassung(adresse)).contains("   50%-Aufschlag : true");
            }

            @Test
            void givenAdresseWhoseFlaecheIsOutsideTheAbrechnungszeitraum_thenSaySoInsteadOfTheBlock() {
                final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 15), 5,
                        new BigDecimal("100"), new BigDecimal("100"));
                final Flaechenadresse adresse = new Flaechenadresse("Marienplatz 8", false, List.of(flaeche), 2);

                assertThat(Flaechentext.zusammenfassung(adresse)).isEqualTo(
                        "Marienplatz 8" + NEWLINE
                                + "   Adresse enthält keine Flächen im Abrechnungszeitraum." + NEWLINE
                                + NEWLINE);
            }

            @Test
            void givenAdresseWithoutFlaeche_thenSaySoInsteadOfTheBlock() {
                final Flaechenadresse adresse = new Flaechenadresse("Marienplatz 8", false, List.of(), 0);

                assertThat(Flaechentext.zusammenfassung(adresse)).isEqualTo(
                        "Marienplatz 8" + NEWLINE
                                + "   Adresse enthält keine Flächen im Abrechnungszeitraum." + NEWLINE
                                + NEWLINE);
            }
        }

        @Nested
        class OfFlaeche {

            @Test
            void givenFlaechenreduzierung_thenWriteItWithoutAPlusSign() {
                final Flaeche flaeche = new Flaeche("Position 2", LocalDate.of(2026, 1, 29), LocalDate.of(2026, 3, 31), 5,
                        new BigDecimal("60"), new BigDecimal("-40"));

                assertThat(Flaechentext.zusammenfassung(flaeche)).isEqualTo(
                        "   - Fläche Position 2" + NEWLINE
                                + "        Beginn: 2026-01-29  Ende: 2026-03-31 (62 Tage --> 9 Wochen)" + NEWLINE
                                + "        Gesamtfläche Adresse: 60m²" + NEWLINE
                                + "        Flächenänderung: -40m²" + NEWLINE
                                + "        Gültigkeit: Woche 5-13" + NEWLINE);
            }

            @Test
            void givenFlaecheOutsideTheAbrechnungszeitraum_thenWriteOnlyThat() {
                final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 15), 5,
                        new BigDecimal("100"), new BigDecimal("100"));

                assertThat(Flaechentext.zusammenfassung(flaeche)).isEqualTo(
                        "   - Fläche Position 1" + NEWLINE
                                + "        Gültigkeit liegt nicht im Abrechnungszeitraum." + NEWLINE);
            }
        }
    }

    @Nested
    class Verteilung {

        @Test
        void givenVerteilungOfSeveralAdressen_thenWriteOneLinePerAdresseAndFlaechenzeitindex() {
            final Flaechenwoche woche = new Flaechenwoche(1, new BigDecimal("150"), STUFE_2,
                    List.of(
                            new Flaechenverteilung("Marienplatz 8", false, ZEITINDEX_1, new BigDecimal("100"), "Position 1"),
                            new Flaechenverteilung("Sendlinger Straße 1", true, ZEITINDEX_2, new BigDecimal("50"),
                                    "Position 1, Position 2")),
                    List.of(), "");

            assertThat(Flaechentext.verteilung(woche)).isEqualTo(
                    "    Marienplatz 8: 100m² / Zeitindex 1 (Fläche: Position 1)" + NEWLINE
                            + "    Sendlinger Straße 1: 50m² / Zeitindex 2 (Fläche: Position 1, Position 2) (50% Aufschlag)"
                            + NEWLINE);
        }
    }

    @Nested
    class Gebuehrenstufe {

        @Test
        void givenWoche_thenWriteTheBaustellenflaecheAndTheStufeItReaches() {
            final Flaechenwoche woche = new Flaechenwoche(1, new BigDecimal("150"), STUFE_2, List.of(), List.of(), "");

            assertThat(Flaechentext.gebuehrenstufe(woche)).isEqualTo("150m² --> Gebührenstufe 2" + NEWLINE);
        }
    }

    @Nested
    class GebuehrenstufeOhneWoche {

        @Test
        void givenAbrechnungWithoutWoche_thenLeaveTheStufeOpen() {
            assertThat(Flaechentext.gebuehrenstufeOhneWoche()).isEqualTo("0m² --> " + NEWLINE);
        }
    }

    @Nested
    class Bescheidzeile {

        @Test
        void givenPosten_thenChargeItForEveryWocheOfTheAbschnitt() {
            final Gebuehrenposten posten = new Gebuehrenposten(STUFE_2, false, ZEITINDEX_1, new BigDecimal("100"));

            assertThat(Flaechentext.bescheidzeile(posten, 4)).isEqualTo(
                    "    4 Wochen x 100m² (100m²) x 1,50€ (Gebührenstufe 2 / Zeitindex 1) = 600,00€" + NEWLINE);
        }

        @Test
        void givenPostenWithAufschlagAndAngefangenemQuadratmeter_thenWriteBothOfThem() {
            final Gebuehrenposten posten = new Gebuehrenposten(STUFE_2, true, ZEITINDEX_2, new BigDecimal("99.5"));

            assertThat(Flaechentext.bescheidzeile(posten, 2)).isEqualTo(
                    "    2 Wochen x 100m² (99.5m²) x 3,00€ (Gebührenstufe 2 / Zeitindex 2) = 600,00€ (50%-Aufschlag)" + NEWLINE);
        }
    }

    @Nested
    class Protokoll {

        @Test
        void givenFlaechenabrechnung_thenWriteOneBlockPerWocheAndTheGesamtgebuehr() {
            final Flaechenwoche woche = new Flaechenwoche(1, new BigDecimal("100"), STUFE_2,
                    List.of(new Flaechenverteilung("Marienplatz 8", false, ZEITINDEX_1, new BigDecimal("100"), "Position 1")),
                    List.of(new Gebuehrenposten(STUFE_2, false, ZEITINDEX_1, new BigDecimal("100"))), "");
            final Flaechenabrechnung flaechen = new Flaechenabrechnung(List.of(woche), List.of(), new BigDecimal("150.00"));

            assertThat(Flaechentext.protokoll(flaechen)).isEqualTo(
                    "Flächen" + NEWLINE
                            + "======================" + NEWLINE
                            + "Woche 1" + NEWLINE
                            + "    Marienplatz 8: 100m² / Zeitindex 1 (Fläche: Position 1)" + NEWLINE
                            + "    Baustellenfläche: 100m² --> Gebührenstufe 2" + NEWLINE
                            + "    100m² (100m²) x 1,50€ (Gebührenstufe 2 / Zeitindex 1) = 150,00€" + NEWLINE
                            + "    ==> Die Gebühr für Woche 1 beträgt 150,00€" + NEWLINE
                            + NEWLINE
                            + "====>>>> Die Flächengebühren betragen 150,00€" + NEWLINE);
        }

        @Test
        void givenPostenWithoutGebuehr_thenLeaveItOut() {
            final Flaechenwoche woche = new Flaechenwoche(1, new BigDecimal("100"), STUFE_2,
                    List.of(new Flaechenverteilung("Marienplatz 8", false, ZEITINDEX_1, new BigDecimal("100"), "Position 1")),
                    List.of(
                            new Gebuehrenposten(STUFE_2, false, ZEITINDEX_1, new BigDecimal("100")),
                            new Gebuehrenposten(STUFE_2, false, ZEITINDEX_2, BigDecimal.ZERO)),
                    "");
            final Flaechenabrechnung flaechen = new Flaechenabrechnung(List.of(woche), List.of(), new BigDecimal("150.00"));

            assertThat(Flaechentext.protokoll(flaechen)).doesNotContain("Zeitindex 2");
        }
    }
}
