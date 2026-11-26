package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FlaechenabrechnungServiceTest {

    private static final String NEWLINE = System.lineSeparator();

    @Nested
    class Berechne {

        @Test
        void givenOwnAdressenEndingFirst_thenCloseWithAnAbschnittWithoutGebuehr() {
            final Flaeche eigeneFlaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 25), 1,
                    new BigDecimal("80"), new BigDecimal("80"), false);
            final Flaeche vorgaengerFlaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 5), LocalDate.of(2026, 2, 8), 1,
                    new BigDecimal("120"), new BigDecimal("120"), false);

            final Flaechenabrechnung flaechen = FlaechenabrechnungService.berechne(
                    List.of(new Flaechenadresse("Musterstrasse 50", false, List.of(eigeneFlaeche), 3)),
                    List.of(new Flaechenadresse("Projekt 2026-0001, Marienplatz 8", false, List.of(vorgaengerFlaeche), 5)),
                    5);

            assertThat(flaechen.abschnitte()).last().satisfies(abschnitt -> {
                assertThat(abschnitt.wocheBeginn()).isEqualTo(4);
                assertThat(abschnitt.wocheEnde()).isEqualTo(5);
                assertThat(abschnitt.gebuehr()).isEqualByComparingTo("0");
                assertThat(abschnitt.gebuehrenstufe()).isEqualTo("120m² --> Gebührenstufe 2" + NEWLINE);
                assertThat(abschnitt.grund()).isEqualTo("    Musterstrasse 50: Fläche nicht mehr aktiv (Fläche Position 1)" + NEWLINE);
            });
        }
    }
}
