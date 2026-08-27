package de.muenchen.oss.sonar.backend.projekt.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ProjektAdresseTest {

    private static final LocalDate BEGINN = LocalDate.of(2026, 1, 1);
    private static final LocalDate ENDE = LocalDate.of(2026, 3, 31);

    @Test
    void givenZeitraum_thenDeriveTageUnerlaubteNutzung() {
        final ProjektAdresse adresse = new ProjektAdresse(null, "Marienplatz 8", "Gastronomie", BEGINN, ENDE, null, 0, false);

        assertThat(adresse.tageUnerlaubteNutzung()).isEqualTo(90);
    }

    @Test
    void givenOnlyTage_thenKeepThem() {
        final ProjektAdresse adresse = new ProjektAdresse(null, "Marienplatz 8", "Gastronomie", null, null, 12, 0, false);

        assertThat(adresse.tageUnerlaubteNutzung()).isEqualTo(12);
        assertThat(adresse.unerlaubteNutzungVon()).isNull();
        assertThat(adresse.unerlaubteNutzungBis()).isNull();
    }

    @Test
    void givenNeitherZeitraumNorTage_thenTageAreNull() {
        final ProjektAdresse adresse = new ProjektAdresse(null, "Marienplatz 8", "Gastronomie", null, null, null, 0, false);

        assertThat(adresse.tageUnerlaubteNutzung()).isNull();
    }

    @Test
    void givenAlreadyDerivedTage_thenDerivationIsStable() {
        // Given: what reading a stored Adresse back looks like, period and days both present
        final ProjektAdresse stored = new ProjektAdresse(null, "Marienplatz 8", "Gastronomie", BEGINN, ENDE, null, 0, false);

        final ProjektAdresse reread = new ProjektAdresse(
                null, "Marienplatz 8", "Gastronomie", BEGINN, ENDE, stored.tageUnerlaubteNutzung(), 0, false);

        assertThat(reread.tageUnerlaubteNutzung()).isEqualTo(stored.tageUnerlaubteNutzung());
    }

    @Test
    void givenTageContradictingZeitraum_thenThrow() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ProjektAdresse(null, "Marienplatz 8", "Gastronomie", BEGINN, ENDE, 12, 0, false))
                .withMessageContaining("tageUnerlaubteNutzung");
    }

    @Test
    void givenInvertedZeitraum_thenThrow() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ProjektAdresse(null, "Marienplatz 8", "Gastronomie", ENDE, BEGINN, null, 0, false))
                .withMessageContaining("unerlaubteNutzungBis");
    }

    @Test
    void givenIncompleteZeitraum_thenThrow() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ProjektAdresse(null, "Marienplatz 8", "Gastronomie", BEGINN, null, null, 0, false));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ProjektAdresse(null, "Marienplatz 8", "Gastronomie", null, ENDE, null, 0, false));
    }
}
