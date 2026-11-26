package de.muenchen.oss.sonar.backend.berechnung.domain;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungsArt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungMast;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaeche;
import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.projekt.domain.LinkedProjekt;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BerechnungslaufServiceTest {

    @Nested
    class Berechne {

        @Test
        void givenVorgaengerBeginningAndEndingLater_thenShiftItsErsteWocheAndExtendTheZeitraum() {
            final Projekt projekt = new Projekt(null, "2026-0002", LocalDate.of(2025, 12, 29), LocalDate.of(2026, 1, 25),
                    Projekt.VERWALTUNGSGEBUEHR, List.of(), null, List.of(new LinkedProjekt(UUID.randomUUID(), "2026-0001")), List.of(), false, false);
            final AbrechnungPosition eigenePosition = new AbrechnungPosition(null, LocalDate.of(2025, 12, 29), LocalDate.of(2026, 1, 25),
                    new BigDecimal("8.00"), new BigDecimal("10.00"), new BigDecimal("80.00"));
            final AbrechnungNutzungsobjekt eigenesNutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Musterstrasse", "50", null, null, null, null, null,
                    null, null, null, null, false, List.of(eigenePosition), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, null, 1, null, "1000000001", false, null, null,
                    LocalDate.of(2025, 12, 29), LocalDate.of(2026, 1, 25), AbrechnungsArt.ENDABRECHNUNG, null, false,
                    List.of(eigenesNutzungsobjekt));
            final AbrechnungPosition vorgaengerPosition = new AbrechnungPosition(null, LocalDate.of(2026, 1, 5), LocalDate.of(2026, 2, 1),
                    new BigDecimal("12.00"), new BigDecimal("10.00"), new BigDecimal("120.00"));
            final AbrechnungNutzungsobjekt vorgaengerNutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null,
                    null, null, null, null, false, List.of(vorgaengerPosition), List.of());

            final Berechnungslauf lauf = BerechnungslaufService.berechne(projekt, abrechnung,
                    List.of(new VorgaengerProjekt("2026-0001", List.of(vorgaengerNutzungsobjekt))), LocalDate.of(2026, 2, 1), null,
                    "mustermann");

            assertThat(lauf.vorgaengerFlaechenadressen()).singleElement().satisfies(adresse -> {
                assertThat(adresse.flaechen()).extracting(Flaeche::ersteWoche).containsExactly(2);
                assertThat(adresse.letzteWoche()).isEqualTo(5);
            });
            assertThat(lauf.zeitraum()).isEqualTo(new Massnahmezeitraum(LocalDate.of(2025, 12, 29), LocalDate.of(2026, 2, 1), 5));
        }

        @Test
        void givenVorgaengerWithMasten_thenStretchTheZeitraumButChargeNoUeberspannung() {
            final Projekt projekt = new Projekt(null, "2026-0002", LocalDate.of(2026, 1, 5), LocalDate.of(2026, 2, 1),
                    Projekt.VERWALTUNGSGEBUEHR, List.of(), null, List.of(new LinkedProjekt(UUID.randomUUID(), "2026-0001")), List.of(), false, false);
            final AbrechnungPosition eigenePosition = new AbrechnungPosition(null, LocalDate.of(2026, 1, 5), LocalDate.of(2026, 2, 1),
                    new BigDecimal("8.00"), new BigDecimal("10.00"), new BigDecimal("80.00"));
            final AbrechnungNutzungsobjekt eigenesNutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Musterstrasse", "50", null, null, null, null, null,
                    null, null, null, null, false, List.of(eigenePosition), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, null, 1, null, "1000000001", false, null, null,
                    LocalDate.of(2026, 1, 5), LocalDate.of(2026, 2, 1), AbrechnungsArt.ENDABRECHNUNG, null, false,
                    List.of(eigenesNutzungsobjekt));
            final AbrechnungNutzungsobjekt vorgaengerNutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null,
                    null, null, null, null, false, List.of(),
                    List.of(new AbrechnungMast(null, LocalDate.of(2025, 12, 1), LocalDate.of(2026, 6, 30))));

            final Berechnungslauf lauf = BerechnungslaufService.berechne(projekt, abrechnung,
                    List.of(new VorgaengerProjekt("2026-0001", List.of(vorgaengerNutzungsobjekt))), LocalDate.of(2026, 12, 31), null,
                    "mustermann");

            assertThat(lauf.zeitraum().beginn()).isEqualTo(LocalDate.of(2025, 12, 1));
            assertThat(lauf.zeitraum().ende()).isEqualTo(LocalDate.of(2026, 6, 30));
            assertThat(lauf.ueberspannungsadressen()).isEmpty();
        }
    }
}
