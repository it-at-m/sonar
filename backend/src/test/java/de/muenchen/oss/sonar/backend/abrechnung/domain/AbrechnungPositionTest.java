package de.muenchen.oss.sonar.backend.abrechnung.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AbrechnungPositionTest {

    private static final LocalDate BEGINN = LocalDate.of(2026, 1, 1);
    private static final LocalDate ENDE = LocalDate.of(2026, 3, 31);

    private static AbrechnungPosition positionWith(final LocalDate beginn, final LocalDate ende) {
        return new AbrechnungPosition(null, beginn, ende, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, false, BigDecimal.ZERO);
    }

    @Nested
    class Zeitraum {
        @Test
        void givenEndeBeforeBeginn_thenThrow() {
            assertThatThrownBy(() -> positionWith(ENDE, BEGINN))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void givenEndeOnBeginn_thenAcceptIt() {
            final AbrechnungPosition position = positionWith(BEGINN, BEGINN);

            assertThat(position.ende()).isEqualTo(BEGINN);
        }
    }
}
