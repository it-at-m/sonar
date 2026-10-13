package de.muenchen.oss.sonar.backend.abrechnung.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AbrechnungMastTest {

    private static final LocalDate BEGINN = LocalDate.of(2026, 1, 1);
    private static final LocalDate ENDE = LocalDate.of(2026, 3, 31);

    @Nested
    class Zeitraum {
        @Test
        void givenEndeBeforeBeginn_thenThrow() {
            assertThatThrownBy(() -> new AbrechnungMast(null, ENDE, BEGINN))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void givenEndeOnBeginn_thenAcceptIt() {
            final AbrechnungMast mast = new AbrechnungMast(null, BEGINN, BEGINN);

            assertThat(mast.ende()).isEqualTo(BEGINN);
        }
    }
}
