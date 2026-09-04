package de.muenchen.oss.sonar.backend.abrechnung.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.muenchen.oss.sonar.backend.common.Adressart;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AbrechnungNutzungsobjektTest {

    private static final LocalDate VON = LocalDate.of(2026, 1, 1);
    private static final LocalDate BIS = LocalDate.of(2026, 1, 31);

    private static final AbrechnungPosition POSITION = new AbrechnungPosition(
            null, VON, BIS, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, false, BigDecimal.ZERO);

    @Nested
    class TageUnerlaubteNutzung {
        @Test
        void givenZeitraum_thenDeriveTheTageInclusive() {
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null,
                    VON, BIS, null, null, List.of(POSITION));

            assertThat(nutzungsobjekt.tageUnerlaubteNutzung()).isEqualTo(31);
        }

        @Test
        void givenOnlyTage_thenKeepThem() {
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null,
                    null, null, 12, null, List.of(POSITION));

            assertThat(nutzungsobjekt.tageUnerlaubteNutzung()).isEqualTo(12);
        }

        @Test
        void givenMatchingZeitraumAndTage_thenKeepThem() {
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null,
                    VON, BIS, 31, null, List.of(POSITION));

            assertThat(nutzungsobjekt.tageUnerlaubteNutzung()).isEqualTo(31);
        }

        @Test
        void givenContradictingTage_thenThrow() {
            assertThatThrownBy(() -> new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null,
                    VON, BIS, 12, null, List.of(POSITION))).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void givenNeitherZeitraumNorTage_thenLeaveThemEmpty() {
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null,
                    null, null, null, null, List.of(POSITION));

            assertThat(nutzungsobjekt.tageUnerlaubteNutzung()).isNull();
        }
    }

    @Nested
    class Zeitraum {
        @Test
        void givenOnlyBeginn_thenThrow() {
            assertThatThrownBy(() -> new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null,
                    VON, null, null, null, List.of(POSITION))).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void givenOnlyEnde_thenThrow() {
            assertThatThrownBy(() -> new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null,
                    null, BIS, null, null, List.of(POSITION))).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void givenInvertedZeitraum_thenThrow() {
            assertThatThrownBy(() -> new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null,
                    BIS, VON, null, null, List.of(POSITION))).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class Positionen {
        @Test
        void givenNoPositionen_thenReturnAnEmptyList() {
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null,
                    null, null, null, null, null);

            assertThat(nutzungsobjekt.positionen()).isEmpty();
        }
    }
}
