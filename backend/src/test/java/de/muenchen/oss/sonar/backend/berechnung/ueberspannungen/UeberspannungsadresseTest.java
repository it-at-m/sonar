package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UeberspannungsadresseTest {

    @Nested
    class EarliestStart {

        @Test
        void givenMasten_thenTakeTheEarliestOfThem() {
            final Mast spaeter = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3);
            final Mast frueher = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 30), 1);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(spaeter, frueher), 6);

            assertThat(adresse.earliestStart()).contains(LocalDate.of(2026, 1, 1));
        }

        @Test
        void givenNoMasten_thenGiveNothing() {
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(), 0);

            assertThat(adresse.earliestStart()).isEmpty();
        }
    }

    @Nested
    class LatestEnd {

        @Test
        void givenMasten_thenTakeTheLatestOfThem() {
            final Mast kurz = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28), 1);
            final Mast lang = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(kurz, lang), 6);

            assertThat(adresse.latestEnd()).contains(LocalDate.of(2026, 6, 30));
        }

        @Test
        void givenNoMasten_thenGiveNothing() {
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(), 0);

            assertThat(adresse.latestEnd()).isEmpty();
        }
    }
}
