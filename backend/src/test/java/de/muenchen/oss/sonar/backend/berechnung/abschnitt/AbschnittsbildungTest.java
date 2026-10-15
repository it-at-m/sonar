package de.muenchen.oss.sonar.backend.berechnung.abschnitt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AbschnittsbildungTest {

    private static final boolean MIT_GRUND = true;
    private static final boolean OHNE_GRUND = false;

    @Nested
    class Abschnitte {

        @Test
        void givenNoZwischenabrechnung_thenKeepAllPeriodenInOneAbschnitt() {
            final List<Abschnitt> abschnitte = Abschnittsbildung.abschnitte(List.of("", "", ""), OHNE_GRUND);

            assertThat(abschnitte).containsExactly(new Abschnitt(1, 3, ""));
        }

        @Test
        void givenASingleZwischenabrechnung_thenKeepAllPeriodenInOneAbschnittWithoutGrund() {
            final List<Abschnitt> abschnitte = Abschnittsbildung.abschnitte(List.of("Beginn", "", ""), OHNE_GRUND);

            assertThat(abschnitte).containsExactly(new Abschnitt(1, 3, ""));
        }

        @Test
        void givenASingleZwischenabrechnung_thenNameItsGrundWhenTheCallerAsksForIt() {
            final List<Abschnitt> abschnitte = Abschnittsbildung.abschnitte(List.of("Beginn", "", ""), MIT_GRUND);

            assertThat(abschnitte).containsExactly(new Abschnitt(1, 3, "Beginn"));
        }

        @Test
        void givenASecondZwischenabrechnung_thenCloseTheAbschnittBeforeIt() {
            final List<Abschnitt> abschnitte = Abschnittsbildung.abschnitte(List.of("Beginn", "", "Wechsel", ""), OHNE_GRUND);

            assertThat(abschnitte).containsExactly(new Abschnitt(1, 2, ""), new Abschnitt(3, 4, "Wechsel"));
        }

        @Test
        void givenSeveralZwischenabrechnungen_thenGiveEachAbschnittTheGrundThatOpenedIt() {
            final List<Abschnitt> abschnitte = Abschnittsbildung.abschnitte(List.of("Beginn", "", "Zweiter", "", "Dritter", ""), OHNE_GRUND);

            assertThat(abschnitte).containsExactly(
                    new Abschnitt(1, 2, ""),
                    new Abschnitt(3, 4, "Zweiter"),
                    new Abschnitt(5, 6, "Dritter"));
        }

        @Test
        void givenZwischenabrechnungenInConsecutivePerioden_thenCutAnAbschnittPerPeriode() {
            final List<Abschnitt> abschnitte = Abschnittsbildung.abschnitte(List.of("Beginn", "Zweiter", "Dritter"), OHNE_GRUND);

            assertThat(abschnitte).containsExactly(
                    new Abschnitt(1, 1, ""),
                    new Abschnitt(2, 2, "Zweiter"),
                    new Abschnitt(3, 3, "Dritter"));
        }

        @Test
        void givenZwischenabrechnungInTheLastPeriode_thenCutAnAbschnittForThatPeriodeAlone() {
            final List<Abschnitt> abschnitte = Abschnittsbildung.abschnitte(List.of("Beginn", "", "", "Wechsel"), OHNE_GRUND);

            assertThat(abschnitte).containsExactly(new Abschnitt(1, 3, ""), new Abschnitt(4, 4, "Wechsel"));
        }
    }
}
