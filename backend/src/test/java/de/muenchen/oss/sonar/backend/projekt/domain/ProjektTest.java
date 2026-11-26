package de.muenchen.oss.sonar.backend.projekt.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProjektTest {

    private static final LocalDate BEGINN = LocalDate.of(2026, 1, 1);
    private static final LocalDate ENDE = LocalDate.of(2026, 3, 31);

    @Test
    void givenInvertedAbrechnungszeitraum_thenThrow() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Projekt(null, "2026-0001", ENDE, BEGINN, Projekt.VERWALTUNGSGEBUEHR, null, null, List.of(), List.of(), false, false))
                .withMessageContaining("abrechnungEnde");
    }

    @Test
    void givenNullAdressen_thenEmptyList() {
        final Projekt projekt = new Projekt(null, "2026-0001", BEGINN, ENDE, Projekt.VERWALTUNGSGEBUEHR, null, null, List.of(), List.of(), false, false);

        assertThat(projekt.adressen()).isEmpty();
    }

    @Test
    void givenNullVorgaengerProjekte_thenEmptyList() {
        final Projekt projekt = new Projekt(null, "2026-0001", BEGINN, ENDE, Projekt.VERWALTUNGSGEBUEHR, List.of(), null, null, List.of(), false, false);

        assertThat(projekt.vorgaengerProjekte()).isEmpty();
    }

    @Test
    void givenNullNachfolgerProjekte_thenEmptyList() {
        final Projekt projekt = new Projekt(null, "2026-0001", BEGINN, ENDE, Projekt.VERWALTUNGSGEBUEHR, List.of(), null, List.of(), null, false, false);

        assertThat(projekt.nachfolgerProjekte()).isEmpty();
    }

    @Test
    void givenLinkedProjekteChangedByTheCaller_thenKeepTheProjektePassedIn() {
        final LinkedProjekt vorgaengerProjekt = new LinkedProjekt(UUID.randomUUID(), "2026-0001");
        final LinkedProjekt nachfolgerProjekt = new LinkedProjekt(UUID.randomUUID(), "2026-0003");
        final List<LinkedProjekt> vorgaengerProjekte = new ArrayList<>(List.of(vorgaengerProjekt));
        final List<LinkedProjekt> nachfolgerProjekte = new ArrayList<>(List.of(nachfolgerProjekt));
        final Projekt projekt = new Projekt(null, "2026-0002", BEGINN, ENDE, Projekt.VERWALTUNGSGEBUEHR, List.of(),
                vorgaengerProjekt.id(), vorgaengerProjekte, nachfolgerProjekte, false, false);

        vorgaengerProjekte.clear();
        nachfolgerProjekte.clear();

        assertThat(projekt.vorgaengerProjekte()).containsExactly(vorgaengerProjekt);
        assertThat(projekt.nachfolgerProjekte()).containsExactly(nachfolgerProjekt);
    }
}
