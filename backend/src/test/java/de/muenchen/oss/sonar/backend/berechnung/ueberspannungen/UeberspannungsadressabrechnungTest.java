package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UeberspannungsadressabrechnungTest {

    @Nested
    class Abschnitte {

        @Test
        void givenNoAbschnitte_thenReturnAnEmptyList() {
            final Ueberspannungsadressabrechnung abrechnung = new Ueberspannungsadressabrechnung(
                    "Marienplatz 8", "", BigDecimal.ZERO, null);

            assertThat(abrechnung.abschnitte()).isEmpty();
        }
    }
}
