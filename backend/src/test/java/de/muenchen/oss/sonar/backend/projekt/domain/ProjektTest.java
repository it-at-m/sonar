package de.muenchen.oss.sonar.backend.projekt.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ProjektTest {

    private static final LocalDate BEGINN = LocalDate.of(2026, 1, 1);
    private static final LocalDate ENDE = LocalDate.of(2026, 3, 31);

    @Test
    void givenInvertedAbrechnungszeitraum_thenThrow() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Projekt(null, "2026-0001", ENDE, BEGINN, null))
                .withMessageContaining("abrechnungEnde");
    }

    @Test
    void givenNullAdressen_thenEmptyList() {
        final Projekt projekt = new Projekt(null, "2026-0001", BEGINN, ENDE, null);

        assertThat(projekt.adressen()).isEmpty();
    }
}
