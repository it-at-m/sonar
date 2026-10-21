package de.muenchen.oss.sonar.backend.berechnung.abschnitt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AbschnittTest {

    @Nested
    class Dauer {

        @Test
        void givenAbschnittOverSeveralPerioden_thenCountBothEnds() {
            assertThat(new Abschnitt(3, 6, "").dauer()).isEqualTo(4);
        }

        @Test
        void givenAbschnittOverASinglePeriode_thenCountIt() {
            assertThat(new Abschnitt(3, 3, "").dauer()).isEqualTo(1);
        }
    }
}
